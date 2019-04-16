package model;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class People {
    private final ArrayList<Person> people = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    ReentrantReadWriteLock getLock() {
        return lock;
    }
    
    ArrayList<Person> getPeople() {
        lock.readLock().lock();
        try {
            return people;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    ArrayList<Person> getPeople(int floor) {
        ArrayList<Person> result = new ArrayList<>();
        Person person;
        lock.writeLock().lock();
        try {
            ListIterator<Person> li = people.listIterator();
            while (li.hasNext()) {
                person = li.next();
                if (person.call(floor)) {
                    li.remove();
                    result.add(person);
                }
            }
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    int size() {
        return people.size();
    }
    
    Stream<Person> stream() {
        return people.stream();
    }
    
    public boolean addPerson(Person person) {
        lock.writeLock().lock();
        try {
            people.add(person);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }
    
    boolean isEmpty() {
        lock.readLock().lock();
        try {
            return people.size() == 0;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public String toString() {
        return people.toString();
    }
}
