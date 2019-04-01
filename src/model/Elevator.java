package model;

import com.oocourse.elevator1.PersonRequest;
import controller.Controller;
import view.Output;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Elevator implements Runnable {
    private static final ArrayList<Elevator> elevators = new ArrayList<>();
    
    private int floor = Controller.ELEVATOR_INIT_POS;
    private Status status = Status.NULL;
    private final ReentrantReadWriteLock statusLock
            = new ReentrantReadWriteLock();
    
    private final PeopleIn in;
    private final ReentrantReadWriteLock peopleInLock;
    
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
                    if (personRequest.getToFloor() != getFloor()) {
                        continue;
                    }
                    li.remove();
                    Output.out(personRequest);
                }
                getLock().writeLock().unlock();
                statusLock.readLock().unlock();
            }
        }
        
        Thread start() {
            return super.start("people in elevator #" + getNum());
        }
    }
    
    public static ArrayList<Elevator> getElevators() {
        return elevators;
    }
    
    public Elevator() {
        elevators.add(this);
        in = new PeopleIn();
        peopleInLock = in.getLock();
    }
    
    public boolean inStatus(Status status) {
        statusLock.readLock().lock();
        try {
            return this.status == status;
        } finally {
            statusLock.readLock().unlock();
        }
    }
    
    public void setStatus(Status status) {
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
        Controller controller = Controller.getInstance();
        while (controller.isInputAlive()
                || Controller.running() || !in.empty()) {
            setStatus(Status.NULL); // in order for controller.stop to run
            if (controller.stop(this) || in.requestOut()) {
                Output.open(this);
                setStatus(Status.OPEN);
                Controller.sleep(Controller.ELEVATOR_DOOR_TIME);
                Controller.sleep(Controller.ELEVATOR_DOOR_TIME);
                setStatus(controller.direction(this));
                Output.close(this);
            } else {
                setStatus(controller.direction(this));
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
            Controller.sleep(Controller.ELEVATOR_SPEED);
        }
    }
}
