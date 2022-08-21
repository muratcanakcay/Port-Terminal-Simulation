package classes;

// TODO: implement container class properly
public class Container {

    private String destination;
    private String name;

    public Container(String destination, String name) {
        this.destination = destination;
        this.name = name;
    }

    public Container() {
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
