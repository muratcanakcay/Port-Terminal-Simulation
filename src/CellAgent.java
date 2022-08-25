import classes.AgentUtils;
import classes.Cell;
import jade.core.Agent;

public class CellAgent extends Agent
{
    private Cell cell;

    @Override
    protected void setup()
    {
        Object[] CellArgs = getArguments();
        int row = Integer.parseInt((String)CellArgs[0]);
        int column = Integer.parseInt((String)CellArgs[1]);
        int stackSize = Integer.parseInt((String)CellArgs[2]);
        cell = new Cell(row, column, stackSize);

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "CellAgent", "Cell" + row + ":" + column);

        AgentUtils.Gui.Send(this, "console", String.format("[%s] Agent is registered to DF.", cell.getName()));
    }
}
