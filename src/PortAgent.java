import classes.AgentUtils;
import classes.Port;
import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import static classes.AgentUtils.searchDF;

public class PortAgent extends Agent
{
    private Port port;

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "PortAgent", "PortAgent");

        // get the GuiAgent AID from DF
        jade.core.AID GuiAgent = searchDF(this, "GuiAgent", "GuiAgent")[0].getName();
        System.out.println("[PortAgent] Received GuiAgent from DF: " + GuiAgent.toString());

        Object[] PortArgs = getArguments();
        int rows = Integer.parseInt((String)PortArgs[0]);
        int columns = Integer.parseInt((String)PortArgs[1]);
        int stackSize = Integer.parseInt((String)PortArgs[2]);
        port = new Port(rows, columns, stackSize);

        System.out.println("[PortAgent] Rows: " + port.getRows() + " Columns: " + port.getColumns() + " StackSize: " + port.getStackSize());

        AgentContainer ac = getContainerController();

        // Create the cell agents of the container storage
        try
        {
            for (int r = 0; r < rows; ++r)
            {
                for (int c = 0; c < columns; ++c)
                {
                    Object[] CellArgs = {String.valueOf(r), String.valueOf(c), String.valueOf(port.getStackSize())};
                    AgentController Cell = ac.createNewAgent("Cell" + r + ":" + c, "CellAgent", CellArgs);
                    Cell.start();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
