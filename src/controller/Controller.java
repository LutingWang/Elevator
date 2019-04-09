package controller;

import model.Elevator;
import model.People;
import model.Person;
import view.Input;
import view.Output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

// TODO: checkstyle
public class Controller {
    public static final boolean DEBUG = false; // TODO: turn off
    public static final boolean DEBUG_REDIR_OUTPUT = DEBUG && false;
    
    public static final List<Integer> FLOORS = Arrays.asList(-3, -2, -1,
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    public static final int ELEVATOR_INIT_POS = FLOORS.indexOf(1);
    public static final int ELEVATOR_NUM = 1;
    public static final int ELEVATOR_SPEED = 400; // milli-sec per floor
    public static final int ELEVATOR_DOOR_TIME = 400; // open and close
    //public static final int ELEVATOR_OPEN_TIME = 200;
    //public static final int ELEVATOR_CLOSE_TIME = 200;
    
    public static final int TOTAL_FLOORS = FLOORS.size();
    private static final People out = new People();
    private static boolean inputAlive = true;
    
    public static People getOut() {
        return out;
    }
    
    public static void setInputAlive(boolean newValue) {
        inputAlive = newValue;
        for (Elevator elevator : Elevator.elevators) {
            elevator.signalAll("noDirection");
        }
    }
    
    public static void newPerson(Person person) {
        out.addPerson(person);
        for (Elevator elevator : Elevator.elevators) {
            elevator.signalAll("notNull");
        }
    }
    
    public static boolean running() {
        out.getLock().readLock().lock();
        try {
            return !out.empty() || inputAlive;
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
        Thread thread = new Thread(Input.getInstance(), "Input");
        thread.setUncaughtExceptionHandler(new MyExceptionHandler());
        Output.init(); // start timing right before input
        thread.start();
    }
}
