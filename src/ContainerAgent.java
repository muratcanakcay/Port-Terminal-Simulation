import classes.AgentUtils;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;

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
