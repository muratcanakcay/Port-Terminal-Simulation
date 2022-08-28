package classes;

// TODO: implement container class properly
public class Container
{

    private String destination;
    private final String name;

    public Container(String name, String destination)
    {
        this.destination = destination;
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getContainerName() {
        return name;
    }


}
