package controller;

import TimedTools.TimedInput;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ControllerTest {
    
    @Test
    public void main() throws IOException, InterruptedException {
        String dependencies = "D:\\OO\\Homework\\Elevator\\Elevator\\elevator-input-hw1-1.3-jar-with-dependencies.jar;"
                + "D:\\OO\\Homework\\Elevator\\Elevator\\timable-output-1.0-raw-jar-with-dependencies.jar; ";
        File input = new File("./test/datacheck_in.txt");
        File output = new File("./test/datacheck_out.txt");
        new TimedInput(Controller.class, input, output, dependencies).test().close();
    }
}