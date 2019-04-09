package model;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class People {
    private final ArrayList<Person> people = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    public ReentrantReadWriteLock getLock() {
        return lock;
    }
    
    public Stream<Person> stream() {
        return people.stream();
    }
    
    public ArrayList<Person> getPeople(Predicate<Person> fun) {
        ArrayList<Person> result = new ArrayList<>();
        Person pr;
        lock.writeLock().lock();
        try {
            ListIterator<Person> li = people.listIterator();
            while (li.hasNext()) {
                pr = li.next();
                if (fun.test(pr)) {
                    li.remove();
                    result.add(pr);
                }
            }
            return result;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void addPerson(Person person) {
        lock.writeLock().lock();
        try {
            people.add(person);
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
    
    @Override
    public String toString() {
        return people.toString();
    }
}
