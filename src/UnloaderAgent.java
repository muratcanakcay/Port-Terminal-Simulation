import classes.AgentUtils;
import classes.Utils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
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

        // agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "UnloaderAgent", name);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        if (ships.length != 1)
        {
            AgentUtils.Gui.Send(this, "console-error", "There's " + ships.length + " ship(s) with the name: " + shipName);
            doDelete();
        }

        shipAgent = ships[0].getName();

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
                        if (Objects.equals(msg.getContent(), "All containers unloaded")) {  } // TODO: implement agent termination, let port know and thus order ship to leave
                        else { requestContainerDestination(msg.getContent()); }
                        break;
                    case "container-destination":
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void requestContainerDestination(String containerName)
    {
        AgentUtils.Gui.Send(this, "console-error", "Placed container " + containerName);

        AID containerAgent = AgentUtils.searchDF(this, "ContainerAgent", containerName)[0].getName();
        AgentUtils.SendMessage(this, containerAgent, ACLMessage.REQUEST, "unloader-request-destination", "What is your destination?");

        addBehaviour(RequestContainerFromShip); // TODO: this should actually be called when the current container is finally handed to the crane
    }


    abstract void unloadAction();

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
