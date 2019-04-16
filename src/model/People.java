package model;

import model.Person;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class People {
    private final ArrayList<Person> people = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public ReentrantReadWriteLock getLock() {
        return lock;
    }
    
    public ArrayList<Person> getPeople() {
        lock.readLock().lock();
        try {
            return people;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public int size() {
        return people.size();
    }
    
    public Stream<Person> stream() {
        return people.stream();
    }
    
    public ArrayList<Person> getPeople(int floor) {
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
    
    public boolean addPerson(Person person) {
        lock.writeLock().lock();
        try {
            people.add(person);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }
    
    public boolean isEmpty() {
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
