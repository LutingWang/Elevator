package model;

import com.oocourse.elevator3.PersonRequest;
import view.Output;

import static controller.Controller.FLOORS;

public class Person extends PersonRequest {
    private int floor; // cur floor or cur dst
    private int floorCache;
    
    private Elevator elevator = null;
    
    public Person(PersonRequest pr) {
        super(FLOORS.indexOf(pr.getFromFloor()),
                FLOORS.indexOf(pr.getToFloor()), pr.getPersonId());
        floor = getFromFloor();
    }
    
    int getFloor() {
        return floor;
    }
    
    void cacheFloor(int floor) {
        floorCache = floor;
    }
    
    boolean call(int floor) {
        return this.floor == floor;
    }

    void getIn(Elevator elevator) {
        if (elevator.getPeopleIn().addPerson(this)) {
            Output.in(this, elevator);
            floor = floorCache;
            this.elevator = elevator;
        } else {
            Manager.arrangePerson(this);
        }
    }
    
    void getOut() {
        Output.out(this, elevator);
        if (floor != getToFloor()) {
            elevator = null;
            Manager.arrangePerson(this);
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + "-CALL-" + floor + "-CACHING-" + floorCache;
    }
}
