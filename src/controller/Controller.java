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

// TODO: checkstyle
public class Controller {
    public static final boolean DEBUG = false; // TODO: turn off
    public static final boolean DEBUG_REDIR_OUTPUT = DEBUG && true;
    public static final boolean DEBUG_DIRECTION = DEBUG && false;
    public static final boolean DEBUG_PEOPLEOUT = DEBUG && false;
    public static final boolean DEBUG_PEOPLEIN = DEBUG && false;
    public static final boolean DEBUG_ELEVATOR_CONTROLLER = DEBUG && false;
    
    public static final int ELEVATOR_INIT_POS = 1;
    public static final int ELEVATOR_NUM = 1;
    public static final int ELEVATOR_SPEED = 500; // milli-sec per floor
    public static final int ELEVATOR_DOOR_TIME = 250; // open or close
    
    public static final int TOTAL_FLOORS = 15;
    
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
        
        public Thread start() {
            return super.start("people out");
        }
    }
    
    private static final PeopleOut out = new PeopleOut();
    
    private static boolean inputAlive = true;
    
    static People getOut() {
        return out;
    }
    
    public static void setInputAlive(boolean newValue) {
        inputAlive = newValue;
    }
    
    public static void newRequest(PersonRequest personRequest) {
        out.addPersonRequest(personRequest);
    }
    
    public static boolean running() {
        out.getLock().readLock().lock();
        try {
            return !out.empty() || inputAlive;
        } finally {
            out.getLock().readLock().unlock();
        }
    }
    
    public static boolean requestStop(int floor) {
        out.getLock().readLock().lock();
        try {
            return out
                    .stream()
                    .anyMatch(pr -> pr.getFromFloor() == floor);
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
        for (int i = 0; i < ELEVATOR_NUM; i++) {
            new Elevator().start();
        }
        out.start();
        Thread thread = new Thread(Input.getInstance(), "Input");
        thread.setUncaughtExceptionHandler(new MyExceptionHandler());
        Output.init(); // start timing right before input
        thread.start();
    }
}
