import classes.AgentUtils;
import classes.Cell;
import jade.core.Agent;

public class CellAgent extends Agent
{
    private Cell cell;

    @Override
    protected void setup()
    {
        Object[] PortArgs = getArguments();
        int row = Integer.parseInt((String)PortArgs[0]);
        int column = Integer.parseInt((String)PortArgs[1]);
        int stackSize = Integer.parseInt((String)PortArgs[2]);
        cell = new Cell(row, column, stackSize);

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "CellAgent", "CellAgent(" + row + "," + column + ")");

        System.out.printf("[%s] Agent is registered to DF.\n", cell.getName());
    }
}
