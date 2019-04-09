package model;

import com.sun.istack.internal.NotNull;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class RedefinableAttr<T> {
    private T attr = null;
    private final Supplier<T> supplier;
    
    public RedefinableAttr(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    
    public void clear() {
        attr = null;
    }
    
    public T cache() {
        attr = supplier.get();
        return attr;
    }
    
    public boolean isPresent() {
        return attr != null;
    }
    
    public T peek() {
        if (attr == null) {
            attr = supplier.get();
        }
        return attr;
    }
    
    public T get() {
        if (attr == null) {
            return supplier.get();
        }
        try {
            return attr;
        } finally {
            attr = null;
        }
    }
}
