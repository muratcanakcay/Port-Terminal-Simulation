package classes;

import jade.core.AID;

public class Crane
{
    private CraneStatus status = CraneStatus.IDLE;
    String name;
    AID container = null;

    public Crane(String craneName_)
    {
        name = craneName_;
    }

    public String getName() { return name; }
    public CraneStatus getStatus() { return status; }
    public AID getContainer() { return container; }
}
