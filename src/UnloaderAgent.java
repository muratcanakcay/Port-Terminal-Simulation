import classes.AgentUtils;
import classes.Utils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

public abstract class UnloaderAgent extends Agent
{
    String name;
    AID shipAgent;

    @Override
    protected void setup() {
        Object[] UnloaderArgs = getArguments();
        String name = (String) UnloaderArgs[0];
        String shipName = (String) UnloaderArgs[1];

        DFAgentDescription[] ships = AgentUtils.searchDF(this, "shipAgent", shipName);

        if (ships.length != 1) {
            AgentUtils.Gui.Send(this, "console-error", "There's " + ships.length + " ship(s) with the name: " + shipName);
            return; // TODO: destroy agent gracefully if no ship is found (is it necessary)?
        }

        shipAgent = ships[0].getName();

        // agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "UnloaderAgent", name);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        addBehaviour(unloadBehaviour);
    }

    Behaviour unloadBehaviour = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            unloadAction();
            block(10 / Utils.Clock.GetSimulationSpeed()); // TODO: maybe remove after unloaders are properly implemented?
        }
    };

    abstract void unloadAction();
}
