package view;

import com.oocourse.TimableOutput;
import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.PersonRequest;
import controller.AutoStart;
import controller.Controller;
import model.Person;

import java.io.IOException;

/**
 * Singleton pattern
 */
public class Input implements AutoStart {
    private static Input instance = new Input();
    
    private Input() {}
    
    public static Input getInstance() {
        return instance;
    }
    
    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput();
        while (true) {
            PersonRequest request = elevatorInput.nextPersonRequest();
            if (request == null) {
                break;
            } else {
                Controller.newPerson(new Person(request));
            }
            if (Controller.DEBUG) {
                TimableOutput.println(">>>input " + request);
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Controller.setInputAlive(false);
    }
    
    @Override
    public String getThreadName() {
        return "input";
    }
}
