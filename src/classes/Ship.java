package classes;

import java.util.Stack;

public class Ship
{
    int arrivalTime;
    int departureTime;
    String name;
    Stack<Container> containers = new Stack<>();

    public Ship(String no_, int arrivalTime_, int departureTime_)
    {
        arrivalTime = arrivalTime_;
        departureTime = departureTime_;
        name = "Ship" + no_;
    }

    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getDepartureTime() { return departureTime; }
    // public Container[] getContainers() { return containers; }
}
