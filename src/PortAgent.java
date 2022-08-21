import jade.core.Agent;

public class PortAgent extends Agent
{
    int rows;
    int columns;
    int stackSize;

    @Override
    protected void setup()
    {
        Object[] PortArgs = getArguments();
        rows = Integer.parseInt((String)PortArgs[0]);
        columns = Integer.parseInt((String)PortArgs[1]);
        stackSize = Integer.parseInt((String)PortArgs[2]);

        System.out.println("[PORTAGENT] Rows: " + rows + " Columns: " + columns + " StackSize: " + stackSize);

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "PortAgent", "PortAgent");
    }
}
