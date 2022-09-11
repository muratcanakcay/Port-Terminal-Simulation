import classes.AgentUtils;
import classes.Crane;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class CraneAgent extends Agent {
    Crane crane;

    @Override
    protected void setup() {
        Object[] CraneArgs = getArguments();
        String craneName = (String)CraneArgs[0];

        crane = new Crane(craneName);

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "CraneAgent", craneName);

        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF. Status is: " + crane.getStatus().toString());
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