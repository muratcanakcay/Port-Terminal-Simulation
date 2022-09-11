import classes.AgentUtils;
import classes.Utils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

import java.util.Objects;

public abstract class UnloaderAgent extends Agent
{
    String name;
    AID shipAgent;

    @Override
    protected void setup() {
        Object[] UnloaderArgs = getArguments();
        name = (String) UnloaderArgs[0];
        String shipName = (String) UnloaderArgs[1];

        DFAgentDescription[] ships = AgentUtils.searchDF(this, "shipAgent", shipName);

        if (ships.length != 1) {
            AgentUtils.Gui.Send(this, "console-error", "There's " + ships.length + " ship(s) with the name: " + shipName);
            takeDown();
            return;
        }

        shipAgent = ships[0].getName();

        // agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "UnloaderAgent", name);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        addBehaviour(RequestContainerFromShip);
        addBehaviour(ReceiveMessages);
    }

    Behaviour RequestContainerFromShip = new OneShotBehaviour(this)
    {
        @Override
        public void action()
        {
            AgentUtils.SendMessage(myAgent, shipAgent, ACLMessage.QUERY_IF, "unloader-request-container", "Requesting next container to unload");
        }
    };

    Behaviour ReceiveMessages = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            ACLMessage msg = receive();

            if (msg != null)
            {
                AgentUtils.Gui.Send(myAgent, "console", "Received message from: " +  msg.getSender().getLocalName() + " : " + msg.getContent());

                switch(msg.getOntology())
                {
                    case "ship-next-container":
                        if (!Objects.equals(msg.getContent(), "All containers unloaded")) { handleContainer(msg.getContent()); }
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void handleContainer(String containerName)
    {
        AgentUtils.Gui.Send(this, "console-error", "Placed container " + containerName);

        addBehaviour(RequestContainerFromShip);
    }


    abstract void unloadAction();

    @Override
    protected void takeDown()
    {
        System.out.println(getAID().getName() + " terminated.");
    }
}
