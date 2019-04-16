package controller;

public class Tools {
    public static void threadMonitor() {
        if (Controller.DEBUG) {
            System.out.println("Thread " +
                    Thread.currentThread().getName() + " awaking");
        }
    }
}
