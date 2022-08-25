import classes.AgentUtils;
import classes.Container;
import jade.core.Agent;

public class ContainerAgent extends Agent
{
    Container container;

    @Override
    protected void setup()
    {
        Object[] ContainerArgs = getArguments();
        container  = (Container)ContainerArgs[0];
//        int departureTime = Integer.parseInt((String)ContainerArgs[2]);


        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "ContainerAgent", container.getName());

        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");
    }
}
