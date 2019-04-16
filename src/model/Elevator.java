package model;

import controller.AutoStart;
import controller.Controller;
import controller.Tools;
import view.Output;

import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Elevator implements AutoStart {
    public static final ArrayList<Elevator> elevators = new ArrayList<>();
    
    private enum Status { OPEN, NULL, RUNNING }
    
    private enum Dir { STOP, UP, DOWN, NULL }
    
    private final class PeopleIn extends People implements AutoStart {
        private final int volume;
        
        PeopleIn(int volume) {
            this.volume = volume;
        }
        
        boolean full() {
            return size() >= volume;
        }
        
        @Override
        public boolean addPerson(Person person) {
            if (full()) {
                return false;
            }
            return super.addPerson(person);
        }
        
        @Override
        public void run() {
            while (true) {
                statusLock.readLock().lock();
                while (status != Status.OPEN
                        || manager.dirCache.peek() != Dir.STOP) {
                    statusLock.readLock().unlock();
                    statusLock.writeLock().lock();
                    try {
                        doorClosed.await();
                        Tools.threadMonitor();
                        statusLock.readLock().lock();
                    } catch (InterruptedException e) {
                        if (Controller.DEBUG) {
                            e.printStackTrace();
                        }
                    } finally {
                        statusLock.writeLock().unlock();
                    }
                }
                Manager.getLock().lock();
                getPeople(floor).forEach(Person::getOut); // in
                out.getPeople(floor).forEach(person ->
                        person.getIn(Elevator.this));
                Manager.getLock().unlock();
                statusLock.readLock().unlock();
                manager.refresh();
            }
        }
        
        @Override
        public String getThreadName() {
            return "people in " + getName();
        }
        
        @Override
        public boolean isDeamon() {
            return true;
        }
    }
    
    private final class SubManager extends Manager implements AutoStart {
        private int floorCache = floor;
        private final RedefinableAttr<Dir> dirCache
                = new RedefinableAttr<>(this::direction);
        private final RedefinableAttr<Boolean> stopCache
                = new RedefinableAttr<>(this::stop);
        private final ReentrantLock attrLock = new ReentrantLock();
        private final Condition notNull = attrLock.newCondition();
        
        Dir getDir() {
            attrLock.lock();
            try {
                return dirCache.get();
            } finally {
                attrLock.unlock();
            }
        }
        
        Boolean getStop() {
            attrLock.lock();
            try {
                return stopCache.get();
            } finally {
                attrLock.unlock();
            }
        }
        
        void refresh() {
            attrLock.lock();
            try {
                dirCache.cache();
                stopCache.cache();
            } finally {
                attrLock.unlock();
            }
        }
        
        Dir direction() {
            if (stop(floor)) {
                return Dir.STOP;
            }
            OptionalInt optionalInt = stopFloors(Elevator.this)
                    .stream()
                    .mapToInt(x -> x)
                    .filter(floor -> floor != Elevator.this.floor)
                    .max();
            if (optionalInt.isPresent()) {
                if (optionalInt.getAsInt() > floor) {
                    return Dir.UP;
                } else {
                    return Dir.DOWN;
                }
            } else {
                return Dir.NULL;
            }
        }
        
        boolean stop(int floor) {
            if (!floors.contains(floor)) {
                return false;
            }
            if (!in.full()) {
                out.getLock().readLock().lock();
                try {
                    if (out.stream()
                            .anyMatch(person -> person.call(floor))) {
                        return true;
                    }
                } finally {
                    out.getLock().readLock().unlock();
                }
            }
            in.getLock().readLock().lock();
            try {
                return in.stream()
                        .anyMatch(person -> person.call(floor));
            } finally {
                in.getLock().readLock().unlock();
            }
        }
        
        Boolean stop() {
            return stop(floorCache);
        }
        
        @Override
        public void run() {
            while (true) {
                attrLock.lock();
                if (dirCache.isPresent() && stopCache.isPresent()) {
                    try {
                        notNull.await();
                        Tools.threadMonitor();
                    } catch (InterruptedException e) {
                        if (Controller.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
                Dir temp = dirCache.cache();
                attrLock.unlock();
                if (temp == Dir.STOP) {
                    statusLock.writeLock().lock();
                    if (status == Status.NULL) {
                        status = Status.RUNNING; // any status but null
                        running.signalAll();
                        noDirection.signalAll();
                    } else if (status == Status.RUNNING) {
                        running.signalAll();
                    } else { // status == Status.OPEN
                        doorClosed.signalAll();
                    }
                    statusLock.writeLock().unlock();
                } else if (temp != Dir.NULL) {
                    if (temp == Dir.UP) {
                        floorCache = floor + 1;
                    } else if (temp == Dir.DOWN) {
                        floorCache = floor - 1;
                    }
                    if (status == Status.NULL) {
                        signalAll("noDirection");
                    }
                }
                attrLock.lock();
                stopCache.cache();
                attrLock.unlock();
            }
        }
        
        @Override
        public String getThreadName() {
            return "sub-manager " + getName();
        }
        
        @Override
        public boolean isDeamon() {
            return true;
        }
    }
    
    private final String name;
    private final int speed;
    private final ArrayList<Integer> floors;
    
    private int floor = Controller.ELEVATOR_INIT_POS;
    private Status status = Status.NULL;
    private final ReentrantReadWriteLock statusLock
            = new ReentrantReadWriteLock();
    private final Condition doorClosed = statusLock.writeLock().newCondition();
    private final Condition doorOpen = statusLock.writeLock().newCondition();
    private final Condition noDirection = statusLock.writeLock().newCondition();
    private final Condition running = statusLock.writeLock().newCondition();
    
    private final PeopleIn in;
    private final People out = new People();
    private final SubManager manager = new SubManager();
    
    private static boolean isAlive() {
        boolean temp = Controller.isInputAlive();
        Manager.getLock().lock();
        for (Elevator elevator : elevators) {
            temp = temp || !elevator.in.isEmpty() || !elevator.out.isEmpty();
        }
        Manager.getLock().unlock();
        return temp;
    }
    
    public Elevator(String name, int speed,
                    ArrayList<Integer> floors, int volume) {
        elevators.add(this);
        this.name = name;
        this.speed = speed;
        this.floors = floors;
        this.in = new PeopleIn(volume);
    }
    
    public String getName() {
        return name;
    }
    
    int getSpeed() {
        return speed;
    }
    
    public int getFloor() {
        return floor;
    }
    
    People getPeopleIn() {
        return in;
    }
    
    People getPeopleOut() {
        return out;
    }
    
    boolean canStop(int floor) {
        return floors.contains(floor);
    }
    
    public void signalAll(String conditionName) {
        switch (conditionName) {
            case "noDirection":
                statusLock.writeLock().lock();
                if (status == Status.NULL) {
                    status = Status.RUNNING;
                    noDirection.signalAll();
                }
                statusLock.writeLock().unlock();
                break;
            case "notNull":
                manager.attrLock.lock();
                manager.notNull.signalAll();
                manager.attrLock.unlock();
                break;
            default:
                throw new RuntimeException();
        }
    }
    
    @Override
    public void run() {
        in.start();
        manager.start();
        try {
            while (isAlive()) {
                statusLock.writeLock().lock();
                Dir temp = manager.getDir();
                if (temp == Dir.NULL) {
                    status = Status.NULL;
                    running.await(speed, TimeUnit.MILLISECONDS);
                    if (status == Status.NULL) {
                        noDirection.await();
                    }
                    status = Status.NULL;
                } else {
                    if (temp == Dir.UP) {
                        floor++;
                        Output.arrive(this);
                    } else if (temp == Dir.DOWN) {
                        floor--;
                        Output.arrive(this);
                    }
                    if (temp == Dir.STOP || manager.getStop()) {
                        Output.open(this);
                        status = Status.OPEN;
                        doorClosed.signalAll();
                        doorOpen.await(Controller.ELEVATOR_DOOR_TIME,
                                TimeUnit.MILLISECONDS);
                        status = Status.RUNNING;
                        Output.close(this);
                    } else {
                        status = Status.RUNNING;
                    }
                    signalAll("notNull");
                    running.await(speed, TimeUnit.MILLISECONDS);
                }
            }
        } catch (InterruptedException e) {
            if (Controller.DEBUG) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
    
    @Override
    public String getThreadName() {
        return "elevator " + getName();
    }
}
