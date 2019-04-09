package view;

import com.oocourse.TimableOutput;
import model.Elevator;
import model.Person;

import static controller.Controller.FLOORS;

public class Output {
    public static void init() {
        TimableOutput.initStartTimestamp();
    }
    
    public static void in(int id, int floor) {
        TimableOutput.println("IN-" + id + "-" + FLOORS.get(floor));
    }
    
    public static void in(Person person) {
        in(person.getPersonId(), person.getFromFloor());
    }
    
    public static void out(int id, int floor) {
        TimableOutput.println("OUT-" + id + "-" + FLOORS.get(floor));
    }
    
    public static void out(Person person) {
        out(person.getPersonId(), person.getToFloor());
    }
    
    public static void open(int floor) {
        TimableOutput.println("OPEN-" + FLOORS.get(floor));
    }
    
    public static void open(Elevator elevator) {
        open(elevator.getFloor());
    }
    
    public static void close(int floor) {
        TimableOutput.println("CLOSE-" + FLOORS.get(floor));
    }
    
    public static void close(Elevator elevator) {
        close(elevator.getFloor());
    }
    
    public static void arrive(int floor) {
        TimableOutput.println("ARRIVE-" + FLOORS.get(floor));
    }
}
