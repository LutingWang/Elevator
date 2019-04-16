package view;

import com.oocourse.TimableOutput;
import model.Elevator;
import model.Person;

import static controller.Controller.FLOORS;

public class Output {
    public static void init() {
        TimableOutput.initStartTimestamp();
    }
    
    public static void in(Person person, Elevator elevator) {
        TimableOutput.println("IN-" + person.getPersonId()
                + "-" + FLOORS.get(elevator.getFloor())
                + "-" + elevator.getName());
    }
    
    public static void out(Person person, Elevator elevator) {
        TimableOutput.println("OUT-" + person.getPersonId()
                + "-" + FLOORS.get(elevator.getFloor())
                + "-" + elevator.getName());
    }
    
    public static void open(Elevator elevator) {
        TimableOutput.println("OPEN-" + FLOORS.get(elevator.getFloor())
                + "-" + elevator.getName());
    }
    
    public static void close(Elevator elevator) {
        TimableOutput.println("CLOSE-" + FLOORS.get(elevator.getFloor())
                + "-" + elevator.getName());
    }
    
    public static void arrive(Elevator elevator) {
        TimableOutput.println("ARRIVE-" + FLOORS.get(elevator.getFloor())
                + "-" + elevator.getName());
    }
}
