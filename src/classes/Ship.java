package classes;

public class Ship
{
    int arrivalTime;
    int departureTime;
    String name;
    Container[] containers;

    public Ship(String shipName_, int arrivalTime_, int departureTime_, Container[] containers_)
    {
        arrivalTime = arrivalTime_;
        departureTime = departureTime_;
        name = shipName_;
        containers = containers_;
    }

    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getDepartureTime() { return departureTime; }
    public Container[] getContainers() { return containers; }
}
