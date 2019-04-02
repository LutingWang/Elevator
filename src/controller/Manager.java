package controller;

import com.oocourse.elevator1.PersonRequest;
import model.People;
import model.Status;

import java.util.OptionalInt;

public class Manager {
    private static final People out = Controller.getOut();
    private final People in;
    
    public Manager(People in) {
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
    
    public Status dirction(int floor) {
        out.getLock().readLock().lock();
        OptionalInt costOut = out.stream()
                .mapToInt(personRequest -> {
                    if (personRequest.getFromFloor() == floor) {
                        return personRequest.getToFloor();
                    }
                    return personRequest.getFromFloor();
                })
                .map(f -> f - floor)
                .map(Manager::floorDiffComp)
                .reduce(Integer::sum);
        out.getLock().readLock().unlock();
        in.getLock().readLock().lock();
        OptionalInt costIn = in.stream()
                .mapToInt(PersonRequest::getToFloor)
                .filter(f -> f != floor)
                .map(f -> f - floor)
                .map(Manager::floorDiffComp)
                .reduce(Integer::sum);
        in.getLock().readLock().unlock();
        if (Controller.DEBUG_DIRECTION) {
            System.out.println(">>>direction@Controller:");
            System.out.println(">>>\tcostIn = " + costIn);
            System.out.println(">>>\tcostOut = " + costOut);
        }
        OptionalInt temp = Tools.add(costIn, costOut);
        if (temp.isPresent()) {
            if (temp.getAsInt() >= 0) {
                return Status.UP;
            } else {
                return Status.DOWN;
            }
        } else {
            return Status.NULL;
        }
    }
}
