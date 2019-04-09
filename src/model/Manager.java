package model;

import controller.Controller;
import controller.Tools;

import java.util.OptionalInt;

class Manager {
    private static final People out = Controller.getOut();
    private final People in;
    
    Manager(People in) {
        this.in = in;
    }
    
    private static int floorDiffComp(int floorDiff) {
        if (floorDiff > 0) {
            return Controller.TOTAL_FLOORS - floorDiff;
        } else if (floorDiff < 0) {
            return -Controller.TOTAL_FLOORS - floorDiff;
        } else {
            if (Controller.DEBUG) {
                throw new RuntimeException();
            } else {
                return 0;
            }
        }
    }
    
    boolean stop(int floor) {
        out.getLock().readLock().lock();
        try {
            if (out
                    .stream()
                    .anyMatch(person -> person.from(floor))) {
                return true;
            }
        } finally {
            out.getLock().readLock().unlock();
        }
        in.getLock().readLock().lock();
        try {
            return in.stream()
                    .anyMatch(person -> person.to(floor));
        } finally {
            in.getLock().readLock().unlock();
        }
    }
    
    Dir direction(int floor) {
        if (stop(floor)) {
            return Dir.STOP;
        }
        OptionalInt cost;
        out.getLock().readLock().lock();
        cost = out.stream()
                .mapToInt(person -> {
                    if (person.from(floor)) {
                        return person.getToFloor();
                    }
                    return person.getFromFloor();
                })
                .distinct()
                .map(f -> f - floor)
                .map(Manager::floorDiffComp)
                .reduce(Integer::sum);
        out.getLock().readLock().unlock();
        cost = Tools.mult(cost, Controller.OUT_AMPL);
        in.getLock().readLock().lock();
        cost = Tools.add(cost, in.stream()
                .mapToInt(Person::getToFloor)
                .filter(f -> f != floor)
                .distinct()
                .map(f -> f - floor)
                .map(Manager::floorDiffComp)
                .reduce(Integer::sum));
        in.getLock().readLock().unlock();
        if (cost.isPresent()) {
            if (cost.getAsInt() >= 0) {
                return Dir.UP;
            } else {
                return Dir.DOWN;
            }
        } else {
            return Dir.NULL;
        }
    }
}
