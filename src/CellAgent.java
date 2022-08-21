import jade.core.Agent;

public class CellAgent extends Agent
{
    int row;
    int column;
    int stackSize;

    @Override
    protected void setup()
    {
        Object[] PortArgs = getArguments();
        row = Integer.parseInt((String)PortArgs[0]);
        column = Integer.parseInt((String)PortArgs[1]);
        stackSize = Integer.parseInt((String)PortArgs[2]);

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "CellAgent", "Cell(" + row + "," + column + ")");

        System.out.printf("[CELLAGENT(%d,%d)] Agent is registered to DF.\n", row, column);
    }
}
