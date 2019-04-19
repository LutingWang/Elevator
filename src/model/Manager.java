package model;

import controller.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static controller.Controller.ELEVATOR_NUM;
import static controller.Controller.TOTAL_FLOORS;

public class Manager {
    private static ReentrantLock lock = new ReentrantLock();
    private static final int[][] path = new int[TOTAL_FLOORS][TOTAL_FLOORS];
    
    static ReentrantLock getLock() {
        return lock;
    }
    
    public static void init() {
        int inf = Integer.MAX_VALUE / 4;
        int size = (ELEVATOR_NUM + 1) * TOTAL_FLOORS;
        int[][] graph = new int[size][size];
        int[][] path = new int[size][size];
        // e0f0, e0f1, ... , e0fn, e1f0, ... , emfn, of0, ... , ofn
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    graph[i][j] = 0;
                } else {
                    graph[i][j] = inf;
                }
                path[i][j] = -1;
            }
        }
        for (int i = 0; i < TOTAL_FLOORS; i++) {
            int ind1 = ELEVATOR_NUM * TOTAL_FLOORS + i;
            for (int j = 0; j < ELEVATOR_NUM; j++) {
                if (Elevator.elevators.get(j).canStop(i)) {
                    int ind2 = j * TOTAL_FLOORS + i;
                    graph[ind1][ind2] = Controller.ELEVATOR_DOOR_TIME;
                    graph[ind2][ind1] = Controller.ELEVATOR_DOOR_TIME;
                }
            }
        }
        for (int i = 0; i < ELEVATOR_NUM; i++) {
            for (int j = 0; j < TOTAL_FLOORS; j++) {
                int ind1 = i * TOTAL_FLOORS + j;
                for (int k = 0; k < j; k++) {
                    int ind2 = i * TOTAL_FLOORS + k;
                    graph[ind1][ind2] = Elevator.elevators.get(i).getSpeed()
                            * (j - k);
                    graph[ind2][ind1] = graph[ind1][ind2];
                }
            }
        }
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (graph[i][j] > graph[i][k] + graph[k][j]) {
                        graph[i][j] = graph[i][k] + graph[k][j];
                        path[i][j] = k;
                    }
                }
            }
        }
        for (int i = ELEVATOR_NUM * TOTAL_FLOORS; i < size; i++) {
            for (int j = ELEVATOR_NUM * TOTAL_FLOORS; j < size; j++) {
                Manager.path[i - ELEVATOR_NUM * TOTAL_FLOORS]
                        [j - ELEVATOR_NUM * TOTAL_FLOORS] = path[i][j];
            }
        }
    }
    
    static List<Integer> stopFloors(Elevator elevator, boolean o) {
        ArrayList<Person> temp = new ArrayList<>();
        temp.addAll(elevator.getPeopleIn().getPeople());
        if (o) { temp.addAll(elevator.getPeopleOut().getPeople()); }
        return temp.stream()
                .map(Person::getFloor).distinct().collect(Collectors.toList());
    }
    
    public static void arrangePerson(Person person) {
        int index = path[person.getFloor()][person.getToFloor()];
        int floor = index % TOTAL_FLOORS;
        if (index >= ELEVATOR_NUM * TOTAL_FLOORS) {
            index = path[person.getFloor()][index % TOTAL_FLOORS];
        } else {
            floor = person.getToFloor();
        }
        Elevator elevator = Elevator.elevators.get(index / TOTAL_FLOORS);
        person.cacheFloor(floor);
        elevator.getPeopleOut().addPerson(person);
        elevator.signalAll("notNull");
    }
}
