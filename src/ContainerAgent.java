import classes.AgentUtils;
import jade.core.Agent;

public class ContainerAgent extends Agent
{
    private String containerName;
    private String destination;

    @Override
    protected void setup()
    {
        Object[] ContainerArgs = getArguments();
        containerName  = (String)ContainerArgs[0];
        destination = (String)ContainerArgs[1];
//        int departureTime = Integer.parseInt((String)ContainerArgs[2]);


        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "ContainerAgent", containerName);

        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");
    }
}
