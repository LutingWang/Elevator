package controller;

import com.oocourse.elevator1.PersonRequest;
import model.Status;
import model.Elevator;
import model.People;
import view.Input;
import view.Output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;

/**
 * Singleton pattern
 */
// TODO: checkstyle, main-try-catch
public class Controller {
    public static final boolean DEBUG = false; // TODO: turn off
    public static final boolean DEBUG_REDIR_OUTPUT = DEBUG && false;
    public static final boolean DEBUG_DIRECTION = DEBUG && false;
    public static final boolean DEBUG_PEOPLEOUT = DEBUG && false;
    public static final boolean DEBUG_PEOPLEIN = DEBUG && false;
    
    public static final int ELEVATOR_INIT_POS = 1;
    public static final int ELEVATOR_NUM = 1;
    public static final int ELEVATOR_SPEED = 500; // milli-sec per floor
    public static final int ELEVATOR_DOOR_TIME = 250; // open or close
    
    public static final int TOTAL_FLOORS = 15;
    
    private static Controller instance = new Controller();
    private static PeopleOut out = new PeopleOut();
    
    private static boolean inputAlive = true;
    
    private static final class PeopleOut extends People {
        @Override
        public void run() {
            while (true) {
                for (Elevator elevator : Elevator.getElevators()) {
                    elevator.getStatusLock().readLock().lock();
                    if (!elevator.inStatus(Status.OPEN)) {
                        elevator.getStatusLock().readLock().unlock();
                        continue;
                    }
                    getLock().writeLock().lock();
                    ListIterator<PersonRequest> li
                            = super.getPeople().listIterator();
                    while (li.hasNext()) {
                        PersonRequest personRequest = li.next();
                        if (personRequest.getFromFloor()
                                != elevator.getFloor()) {
                            continue;
                        }
                        li.remove();
                        elevator.getIn(personRequest);
                    }
                    getLock().writeLock().unlock();
                    elevator.getStatusLock().readLock().unlock();
                }
            }
        }
        
        Thread start() {
            return super.start("people out");
        }
    }
    
    private Controller() {}
    
    public void inputDied() {
        inputAlive = false;
    }
    
    public boolean isInputAlive() {
        return inputAlive;
    }
    
    public static Controller getInstance() {
        return instance;
    }
    
    public void newRequest(PersonRequest personRequest) {
        out.addPersonRequest(personRequest);
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }
    
    public static boolean running() {
        out.getLock().readLock().lock();
        try {
            return !out.empty();
        } finally {
            out.getLock().readLock().unlock();
        }
    }
    
    public Status direction(Elevator elevator) {
        IntUnaryOperator floorDiffComp = floorDiff -> {
            if (floorDiff > 0) {
                return TOTAL_FLOORS - floorDiff;
            } else if (floorDiff < 0) {
                return -TOTAL_FLOORS - floorDiff;
            } else {
                if (DEBUG) { throw new RuntimeException(); }
                else { return 0; }
            }
        };
        out.getLock().readLock().lock();
        final OptionalInt costOut = out
                .stream()
                .mapToInt(PersonRequest::getFromFloor)
                .filter(fromFloor -> fromFloor != elevator.getFloor())
                .map(fromFloor -> fromFloor - elevator.getFloor())
                .map(floorDiffComp)
                .reduce(Integer::sum);
        out.getLock().readLock().unlock();
        elevator.getPeopleInLock().readLock().lock();
        final OptionalInt costIn = elevator
                .getPeopleIn()
                .stream()
                .mapToInt(PersonRequest::getToFloor)
                .filter(toFloor -> toFloor != elevator.getFloor())
                .map(toFloor -> toFloor - elevator.getFloor())
                .map(floorDiffComp)
                .reduce(Integer::sum);
        elevator.getPeopleInLock().readLock().unlock();
        if (costOut.isPresent() || costIn.isPresent()) {
            if (DEBUG_DIRECTION) {
                System.out.println(">>>direction@Controller:");
                System.out.println(">>>\tcostIn = " + costIn);
                System.out.println(">>>\tcostOut = " + costOut);
            }
            int cost = 0;
            if (costOut.isPresent()) {
                cost += costOut.getAsInt();
            }
            if (costIn.isPresent()) {
                cost += costIn.getAsInt();
            }
            if (cost > 0) {
                return Status.UP;
            } else if (cost < 0) {
                return Status.DOWN;
            } else {
                if (DEBUG) {
                    throw new RuntimeException();
                } else {
                    return Status.UP;
                }
            }
        } else {
            return Status.NULL;
        }
    }
    
    public boolean stop(Elevator elevator) {
        out.getLock().readLock().lock();
        try {
            return out
                    .stream()
                    .anyMatch(pr -> pr.getFromFloor() == elevator.getFloor());
        } finally {
            out.getLock().readLock().unlock();
        }
    }
    
    public static void main(String[] args) {
        Thread.currentThread()
                .setUncaughtExceptionHandler(new MyExceptionHandler());
        if (DEBUG_REDIR_OUTPUT) {
            try {
                System.setOut(new PrintStream(
                        new FileOutputStream("./out.txt")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        Output.init();
        Elevator elevator;
        Thread thread;
        for (int i = 0; i < ELEVATOR_NUM; i++) {
            elevator = new Elevator();
            thread = new Thread(elevator, "elevator #" + elevator.getNum());
            thread.setUncaughtExceptionHandler(new MyExceptionHandler());
            thread.start();
        }
        out.start();
        thread = new Thread(Input.getInstance(), "Input");
        thread.setUncaughtExceptionHandler(new MyExceptionHandler());
        thread.start();
    }
}
