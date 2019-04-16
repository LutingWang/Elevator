package controller;

import model.Elevator;
import model.Manager;
import model.Person;
import view.Input;
import view.Output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: checkstyle
public class Controller {
    public static final boolean DEBUG = false; // TODO: turn off
    public static final boolean DEBUG_REDIR_OUTPUT = DEBUG && false;
    
    public static final List<Integer> FLOORS = Arrays.asList(-3, -2, -1, 1,
            2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
    public static final int TOTAL_FLOORS = FLOORS.size();
    
    public static final int ELEVATOR_NUM = 3;
    private static final String[] ELEVATOR_NAMES = {"A", "B", "C"};
    private static final int[] ELEVATOR_VOLUME = {6, 8, 7};
    private static final int[] ELEVATOR_SPEEDS = {400, 500, 600};
    private static final ArrayList<ArrayList<Integer>> ELEVATOR_FLOORS;
    
    static {
        ELEVATOR_FLOORS = new ArrayList<>();
        int[][] temp = {
                {-3, -2, -1, 1, 15, 16, 17, 18, 19, 20},
                {-2, -1, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
                {1, 3, 5, 7, 9, 11, 13, 15}
        };
        ArrayList<Integer> arrayList;
        for (int i = 0; i < ELEVATOR_NUM; i++) {
            arrayList = new ArrayList<>();
            for (int j = 0; j < temp[i].length; j++) {
                arrayList.add(FLOORS.indexOf(temp[i][j]));
            }
            ELEVATOR_FLOORS.add(arrayList);
        }
    }
    
    public static final int ELEVATOR_INIT_POS = FLOORS.indexOf(1);
    public static final int ELEVATOR_DOOR_TIME = 400; // open and close
    // public static final int ELEVATOR_OPEN_TIME = 200;
    // public static final int ELEVATOR_CLOSE_TIME = 200;
    
    private static boolean inputAlive = true;
    
    public static void setInputAlive(boolean newValue) {
        inputAlive = newValue;
        for (Elevator elevator : Elevator.elevators) {
            elevator.signalAll("noDirection");
        }
    }
    
    public static boolean isInputAlive() {
        return inputAlive;
    }
    
    public static void newPerson(Person person) {
        Manager.arrangePerson(person);
    }
    
    public static void main(String[] args) {
        Thread.currentThread()
                .setUncaughtExceptionHandler(AutoStart.eh);
        if (DEBUG_REDIR_OUTPUT) {
            try {
                System.setOut(new PrintStream(
                        new FileOutputStream("./out.txt")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    
        // starting threads
        for (int i = 0; i < ELEVATOR_NUM; i++) {
            new Elevator(ELEVATOR_NAMES[i], ELEVATOR_SPEEDS[i],
                    ELEVATOR_FLOORS.get(i), ELEVATOR_VOLUME[i]).start();
        }
        Manager.init();
        Output.init();
        Input.getInstance().start();
    }
}
