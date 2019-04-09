package model;

import com.oocourse.elevator2.PersonRequest;

import static controller.Controller.FLOORS;

public class Person {
    private PersonRequest pr;
    private int fromFloor;
    private int toFloor;
    private int personId;
    
    public Person(PersonRequest pr) {
        this.pr = pr;
        fromFloor = FLOORS.indexOf(pr.getFromFloor());
        toFloor = FLOORS.indexOf(pr.getToFloor());
        personId = pr.getPersonId();
    }
    
    public int getFromFloor() {
        return fromFloor;
    }
    
    public boolean from(int floor) {
        return fromFloor == floor;
    }
    
    public int getToFloor() {
        return toFloor;
    }
    
    public boolean to(int floor) {
        return toFloor == floor;
    }
    
    public int getPersonId() {
        return personId;
    }
    
    @Override
    public boolean equals(Object obj) {
        return pr.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return pr.hashCode();
    }
    
    @Override
    public String toString() {
        return pr.toString();
    }
}
