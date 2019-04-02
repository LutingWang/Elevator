package model;

import com.oocourse.elevator1.PersonRequest;
import controller.AutoStart;
import controller.MyExceptionHandler;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public abstract class People implements AutoStart {
    private final ArrayList<PersonRequest> people = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public ReentrantReadWriteLock getLock() {
        return lock;
    }
    
    public Stream<PersonRequest> stream() {
        return people.stream();
    }
    
    protected ArrayList<PersonRequest> getPeople() {
        return people;
    }
    
    public void addPersonRequest(PersonRequest personRequest) {
        lock.writeLock().lock();
        try {
            people.add(personRequest);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean empty() {
        lock.readLock().lock();
        try {
            return people.size() == 0;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    protected Thread start(String name) {
        Thread thread = new Thread(this, name);
        thread.setUncaughtExceptionHandler(new MyExceptionHandler());
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
