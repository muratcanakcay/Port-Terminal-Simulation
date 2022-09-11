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
    String currentContainerName;
    String currentDestination;
    AID shipAgent;
    AID portAgent;

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

        // agent gets the portAgent from DF
        portAgent = AgentUtils.searchDF(this, "PortAgent", "PortAgent")[0].getName();
        shipAgent = ships[0].getName();

        addBehaviour(ReceiveMessages);
        addBehaviour(RequestContainerFromShip);
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
                        currentContainerName = msg.getContent();
                        if (Objects.equals(currentContainerName, "All containers unloaded")) {  } // TODO: implement agent termination, let port know and thus order ship to leave
                        else { requestContainerDestination(msg.getContent()); }
                        break;
                    case "container-destination":
                        currentDestination = msg.getContent();
                        requestETAofShipToDestination(currentDestination);
                        break;
                    case "port-shipETA":
                        int shipETA = Integer.parseInt(msg.getContent());
                        if (shipETA < 0) { AgentUtils.Gui.Send(myAgent, "console-error", "There's no ship that will take " + currentContainerName + " to destination " + currentDestination); }
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void requestETAofShipToDestination(String destination)
    {
        AgentUtils.SendMessage(this, portAgent, ACLMessage.REQUEST, "unloader-request-shipETA", destination);

        //AgentUtils.Gui.Send(this, "console-error", "Placed container " + currentContainerName);
        addBehaviour(RequestContainerFromShip); // TODO: this should actually be called when the current container is finally handed to the crane
    }

    private void requestContainerDestination(String containerName)
    {
        AID containerAgent = AgentUtils.searchDF(this, "ContainerAgent", containerName)[0].getName();
        AgentUtils.SendMessage(this, containerAgent, ACLMessage.REQUEST, "unloader-request-destination", "What is your destination?");
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
