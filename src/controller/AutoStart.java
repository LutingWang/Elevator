package controller;

import java.lang.Thread.UncaughtExceptionHandler;

public interface AutoStart extends Runnable {
    UncaughtExceptionHandler eh = (Thread t, Throwable e) -> {
        System.out.println("An exception has been captured");
        System.out.printf("Thread:%s\n", t.getName());
        System.out.printf("Exception: %s: %s:\n",
                e.getClass().getName(), e.getMessage());
        System.out.println("Stack Trace:");
        e.printStackTrace();
        System.out.printf("Thread status:%s\n", t.getState());
    };
    
    String getThreadName();
    
    default boolean isDeamon() {
        return false;
    }
    
    default Thread start() {
        Thread thread = new Thread(this, getThreadName());
        thread.setUncaughtExceptionHandler(eh);
        thread.setDaemon(isDeamon());
        thread.start();
        return thread;
    }
}
