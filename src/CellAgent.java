import classes.AgentUtils;
import classes.Cell;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;

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

        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");
    }

    @Override
    protected void takeDown()
    {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println(getAID().getName() + " terminated.");
    }
}
