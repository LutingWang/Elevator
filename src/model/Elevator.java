package model;

import com.oocourse.elevator1.PersonRequest;
import controller.AutoStart;
import controller.Controller;
import controller.Manager;
import controller.MyExceptionHandler;
import controller.Tools;
import view.Output;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Elevator implements AutoStart {
    private static final ArrayList<Elevator> elevators = new ArrayList<>();
    
    private int floor = Controller.ELEVATOR_INIT_POS;
    private Status status = Status.NULL;
    private final ReentrantReadWriteLock statusLock
            = new ReentrantReadWriteLock();
    
    private final class PeopleIn extends People {
        boolean requestOut() {
            getLock().readLock().lock();
            try {
                return stream()
                        .anyMatch(pr -> pr.getToFloor() == floor);
            } finally {
                getLock().readLock().unlock();
            }
        }
        
        @Override
        public void run() {
            while (true) {
                // using lock to save efficiency
                statusLock.readLock().lock();
                if (status != Status.OPEN) {
                    statusLock.readLock().unlock();
                    continue;
                }
                getLock().writeLock().lock();
                ListIterator<PersonRequest> li = getPeople().listIterator();
                while (li.hasNext()) {
                    PersonRequest personRequest = li.next();
                    if (personRequest.getToFloor() != floor) {
                        continue;
                    }
                    li.remove();
                    Output.out(personRequest);
                }
                getLock().writeLock().unlock();
                statusLock.readLock().unlock();
            }
        }
        
        public Thread start() {
            return super.start("people in " + getName());
        }
    }
    
    private final PeopleIn in;
    private final ReentrantReadWriteLock peopleInLock;
    
    private final class ElevatorController
            extends Controller implements AutoStart {
        private final Manager manager;
        private Boolean stop = null;
        private ReentrantReadWriteLock stopLock = new ReentrantReadWriteLock();
        private Status status = null;
        private ReentrantReadWriteLock statusLock
                = new ReentrantReadWriteLock();
        
        ElevatorController(People in) {
            manager = new Manager(in);
        }
        
        boolean requestStop() {
            stopLock.writeLock().lock();
            try {
                if (stop == null) {
                    stop = in.requestOut() || Controller.requestStop(floor);
                }
                return stop;
            } finally {
                stop = null;
                stopLock.writeLock().unlock();
            }
        }
        
        Status nextStatus() {
            statusLock.writeLock().lock();
            try {
                if (status == null) {
                    status = manager.dirction(floor);
                }
                return status;
            } finally {
                status = null;
                statusLock.writeLock().unlock();
            }
        }
        
        @Override
        public void run() {
            while (true) {
                if (stop == null) {
                    stopLock.writeLock().lock();
                    stop = in.requestOut() || Controller.requestStop(floor);
                    stopLock.writeLock().unlock();
                } else {
                    stop = in.requestOut() || Controller.requestStop(floor);
                }
                if (status == null) {
                    statusLock.writeLock().lock();
                    status = manager.dirction(floor);
                    statusLock.writeLock().unlock();
                } else {
                    status = manager.dirction(floor);
                }
            }
        }
        
        @Override
        public Thread start() {
            Thread thread = new Thread(this, "controller of " + getName());
            thread.setUncaughtExceptionHandler(new MyExceptionHandler());
            thread.setDaemon(true);
            thread.start();
            return thread;
        }
    }
    
    private final ElevatorController controller;
    
    public static ArrayList<Elevator> getElevators() {
        return elevators;
    }
    
    public Elevator() {
        elevators.add(this);
        in = new PeopleIn();
        peopleInLock = in.getLock();
        controller = new ElevatorController(in);
    }
    
    public boolean inStatus(Status status) {
        statusLock.readLock().lock();
        try {
            return this.status == status;
        } finally {
            statusLock.readLock().unlock();
        }
    }
    
    private void setStatus(Status status) {
        statusLock.writeLock().lock();
        try {
            this.status = status;
        } finally {
            statusLock.writeLock().unlock();
        }
    }
    
    public ReentrantReadWriteLock getStatusLock() {
        return statusLock;
    }
    
    public int getNum() {
        return elevators.indexOf(this);
    }
    
    public String getName() {
        return "elevator #" + getNum();
    }
    
    public int getFloor() {
        assert status == Status.OPEN || status == Status.NULL;
        return floor;
    }
    
    public People getPeopleIn() {
        return in;
    }
    
    public ReentrantReadWriteLock getPeopleInLock() {
        return peopleInLock;
    }
    
    public void getIn(PersonRequest personRequest) {
        in.addPersonRequest(personRequest);
        Output.in(personRequest);
    }
    
    @Override
    public void run() {
        in.start();
        controller.start();
        while (Controller.running() || !in.empty()) {
            if (controller.requestStop()) {
                Output.open(this);
                setStatus(Status.OPEN);
                Tools.sleep(2 * Controller.ELEVATOR_DOOR_TIME);
                setStatus(controller.nextStatus());
                Output.close(this);
            } else {
                setStatus(controller.nextStatus());
            }
            /* setStatus is only used before, so there is no problem
             * using status. Instead of inStatus, this is intended to
             * be more efficiency.
             */
            if (status == Status.NULL) {
                continue;
            }
            if (Controller.DEBUG_DIRECTION) {
                System.out.println(status);
            }
            if (status == Status.UP) {
                floor++;
            } else if (status == Status.DOWN) {
                floor--;
            }
            if (Controller.DEBUG) {
                System.out.println(">>>Elevator #"
                        + getNum() + " going " + status + " to " + floor);
            }
            Tools.sleep(Controller.ELEVATOR_SPEED);
        }
    }
    
    @Override
    public Thread start() {
        Thread thread = new Thread(this, getName());
        thread.setUncaughtExceptionHandler(new MyExceptionHandler());
        thread.start();
        return thread;
    }
}
