package model;

import java.util.function.Supplier;

class RedefinableAttr<T> {
    private T attr = null;
    private final Supplier<T> supplier;
    
    RedefinableAttr(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    
    T cache() {
        attr = supplier.get();
        return attr;
    }
    
    boolean isPresent() {
        return attr != null;
    }
    
    T peek() {
        if (attr == null) {
            attr = supplier.get();
        }
        return attr;
    }
    
    T get() {
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
