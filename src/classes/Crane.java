package classes;

public class Crane
{
    private CraneStatus status = CraneStatus.IDLE;
    String name;
    Container container = null;

    public Crane(String craneName_)
    {
        name = craneName_;
    }

    public String getName() { return name; }
    public CraneStatus getStatus() { return status; }
    public Container getContainer() { return container; }
}
