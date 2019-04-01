package controller;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("An exception has been captured");
        System.out.printf("Thread:%s\n", t.getName());
        System.out.printf("Exception: %s: %s:\n",
                e.getClass().getName(), e.getMessage());
        System.out.println("Stack Trace:");
        e.printStackTrace();
        System.out.printf("Thread status:%s\n", t.getState());
    }
}
