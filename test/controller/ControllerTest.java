package controller;

import TimedTools.TimedInput;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ControllerTest {
    
    @Test
    public void main() throws IOException, InterruptedException {
        String dependencies = "D:\\OO\\Homework\\Elevator\\Elevator\\dependencies\\elevator-input-hw3-1.4-jar-with-dependencies.jar;"
                + "D:\\OO\\Homework\\Elevator\\Elevator\\dependencies\\timable-output-1.0-raw-jar-with-dependencies.jar; ";
        File input = new File("./test/datacheck_in.txt");
        File output = new File("./test/datacheck_out.txt");
        new TimedInput(Controller.class, input, output, dependencies).setTimeout(100*1000).test().close();
    }
    
    @Test
    public void temp() throws IOException, InterruptedException {
        String dependencies = "D:\\OO\\Homework\\Elevator\\Elevator\\dependencies\\elevator-input-hw3-1.3-jar-with-dependencies.jar;"
                + "D:\\OO\\Homework\\Elevator\\Elevator\\dependencies\\timable-output-1.0-raw-jar-with-dependencies.jar; ";
        File input = new File("./test/datacheck_in.txt");
        File output = new File("./test/datacheck_out.txt");
        //new TimedInput(Simulator.class, input, output, dependencies).setTimeout(30*1000).test().close();
    }
}