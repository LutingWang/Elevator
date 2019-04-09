package model;

import controller.AutoStart;
import controller.Controller;
import controller.MyExceptionHandler;
import controller.Tools;
import view.Output;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Elevator implements AutoStart {
    public static final ArrayList<Elevator> elevators = new ArrayList<>();
    
    private int floor = Controller.ELEVATOR_INIT_POS;
    private Status status = Status.NULL;
    private final ReentrantReadWriteLock statusLock
            = new ReentrantReadWriteLock();
    private final Condition doorClosed = statusLock.writeLock().newCondition();
    private final Condition doorOpen = statusLock.writeLock().newCondition();
    private final Condition noDirection = statusLock.writeLock().newCondition();
    private final Condition running = statusLock.writeLock().newCondition();
    
    private final class PeopleIn extends People implements AutoStart {
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
                if (Controller.DEBUG) {
                    System.out.println("people in: " + in);
                }
                in.getPeople(person -> person.to(floor))
                        .forEach(Output::out);
                if (Controller.DEBUG) {
                    System.out.println("people out: " + Controller.getOut());
                }
                Controller.getOut()
                        .getPeople(person -> person.from(floor))
                        .forEach(person -> {
                            Output.in(person);
                            in.addPerson(person);
                        });
                statusLock.readLock().unlock();
                manager.refresh();
            }
        }
        
        public Thread start() {
            Thread thread = new Thread(this, "people in " + getName());
            thread.setUncaughtExceptionHandler(new MyExceptionHandler());
            thread.setDaemon(true);
            thread.start();
            return thread;
        }
    }
    
    private final PeopleIn in = new PeopleIn();
    
    private final class SubManager extends Manager implements AutoStart {
        private int floorCache = floor;
        private final RedefinableAttr<Dir> dirCache
                = new RedefinableAttr<>(() ->  {
                    if (Controller.DEBUG) {
                        System.out.println("at " + floor
                                + " dirCache: " + direction(floor));
                    }
                    return direction(floor);
                });
        private final RedefinableAttr<Boolean> stopCache
                = new RedefinableAttr<>(() -> {
                    if (Controller.DEBUG) {
                        System.out.println("at " + floor
                                + " stopCache: " + stop(floorCache));
                    }
                    return stop(floorCache);
                });
        private final ReentrantLock attrLock = new ReentrantLock();
        private final Condition notNull = attrLock.newCondition();
        
        SubManager() {
            super(in);
        }
        
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
        public Thread start() {
            Thread thread = new Thread(this, "sub-manager " + getName());
            thread.setUncaughtExceptionHandler(new MyExceptionHandler());
            thread.setDaemon(true);
            thread.start();
            return thread;
        }
    }
    
    private final SubManager manager = new SubManager();
    
    public Elevator() {
        elevators.add(this);
    }
    
    private String getName() {
        return "elevator #" + elevators.indexOf(this);
    }
    
    public int getFloor() {
        return floor;
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
            while (Controller.running() || !in.empty()) {
                statusLock.writeLock().lock();
                Dir temp = manager.getDir();
                if (temp == Dir.NULL) {
                    status = Status.NULL;
                    running.await(Controller.ELEVATOR_SPEED,
                            TimeUnit.MILLISECONDS);
                    if (status == Status.NULL) {
                        noDirection.await();
                    }
                    status = Status.NULL;
                } else {
                    if (temp == Dir.UP) {
                        floor++;
                        Output.arrive(floor);
                    } else if (temp == Dir.DOWN) {
                        floor--;
                        Output.arrive(floor);
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
                    running.await(Controller.ELEVATOR_SPEED,
                            TimeUnit.MILLISECONDS);
                }
            }
        } catch (InterruptedException e) {
            if (Controller.DEBUG) {
                e.printStackTrace();
            }
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
