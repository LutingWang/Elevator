package view;

import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;
import controller.Controller;

import java.io.IOException;

/**
 * Singleton pattern
 */
public class Input implements Runnable {
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
                Controller.getInstance().newRequest(request);
            }
            if (Controller.DEBUG) {
                System.out.println(">>>Input");
            }
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Controller.getInstance().inputDied();
    }
}
