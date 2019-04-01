package view;

import com.oocourse.TimableOutput;
import com.oocourse.elevator1.PersonRequest;
import model.Elevator;

public class Output {
    public static void init() {
        TimableOutput.initStartTimestamp();
    }
    
    public static void in(int id, int floor) {
        TimableOutput.println("IN-" + id + "-" + floor);
    }
    
    public static void in(PersonRequest personRequest) {
        in(personRequest.getPersonId(), personRequest.getFromFloor());
    }
    
    public static void out(int id, int floor) {
        TimableOutput.println("OUT-" + id + "-" + floor);
    }
    
    public static void out(PersonRequest personRequest) {
        out(personRequest.getPersonId(), personRequest.getToFloor());
    }
    
    public static void open(int floor) {
        TimableOutput.println("OPEN-" + floor);
    }
    
    public static void open(Elevator elevator) {
        open(elevator.getFloor());
    }
    
    public static void close(int floor) {
        TimableOutput.println("CLOSE-" + floor);
    }
    
    public static void close(Elevator elevator) {
        close(elevator.getFloor());
    }
}
