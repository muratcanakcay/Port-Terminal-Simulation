import classes.AgentUtils;
import classes.Crane;
import jade.core.Agent;

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
}