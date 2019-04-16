package controller;

import com.oocourse.TimableOutput;

public class Tools {
    public static void threadMonitor() {
        if (Controller.DEBUG) {
            TimableOutput.println("Thread " +
                    Thread.currentThread().getName() + " awaking");
        }
    }
}
