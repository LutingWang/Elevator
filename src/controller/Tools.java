package controller;

import java.util.Arrays;
import java.util.OptionalInt;

public class Tools {
    private static OptionalInt add(OptionalInt op1, OptionalInt op2) {
        if (!op1.isPresent() && !op2.isPresent()) {
            return OptionalInt.empty();
        }
        int result = 0;
        if (op1.isPresent()) {
            result += op1.getAsInt();
        }
        if (op2.isPresent()) {
            result += op2.getAsInt();
        }
        return OptionalInt.of(result);
    }
    
    public static OptionalInt add(OptionalInt... ops) {
        if (ops.length == 0) {
            return OptionalInt.empty();
        }
        if (ops.length == 1) {
            return ops[0];
        }
        return Arrays.stream(ops).reduce(Tools::add).get();
    }
    
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            if (Controller.DEBUG) {
                e.printStackTrace();
            }
        }
    }
}
