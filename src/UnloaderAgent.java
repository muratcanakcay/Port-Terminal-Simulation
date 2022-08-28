import classes.AgentUtils;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

public class UnloaderAgent extends Agent
{
    String name;
    DFAgentDescription shipAgent;

    @Override
    protected void setup()
    {
        Object[] UnloaderArgs = getArguments();
        String  name = (String)UnloaderArgs[0];
        String  shipName = (String)UnloaderArgs[1];

        DFAgentDescription[] ships = AgentUtils.searchDF(this, "shipAgent", shipName);
        if (ships.length != 1)
        {
            AgentUtils.Gui.Send(this, "console-error", "There's " + ships.length + " ship(s) with the name: " + shipName);
            return; // TODO: destroy agent gracefully if no ship is found!
        }

        shipAgent = ships[0];
    };


}
