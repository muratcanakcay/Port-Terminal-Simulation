import classes.AgentUtils;
import classes.Container;
import classes.ShipStatus;
import classes.Utils;
import classes.Utils.Clock;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

public class ShipAgent extends Agent
{
    private ShipStatus status = ShipStatus.APPROACHING;
    private int arrivalTime;
    private int departureTime;
    private String shipName;
    private Container[] containers;
    private DFAgentDescription portAgent;

    @Override
    protected void setup()
    {
        Object[] ShipArgs = getArguments();
        shipName = (String)ShipArgs[0];
        arrivalTime = Integer.parseInt((String)ShipArgs[1]);
        departureTime = Integer.parseInt((String)ShipArgs[2]);
        containers = (Container[])ShipArgs[3];

        // agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "ShipAgent", shipName);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        // agent gets the portAgent from DF
        portAgent = AgentUtils.searchDF(this, "PortAgent", "PortAgent")[0];

        addBehaviour(CheckArrival);
        addBehaviour(ReceiveMessages);
    }

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
                    case "port-order-wait":
                        status = ShipStatus.WAITING;
                        AgentUtils.Gui.Send(myAgent, "ship-waiting", status.toString() + ":" + containers.length + ":" + arrivalTime + ":" + departureTime);
                        AgentUtils.Gui.Send(myAgent, "console", "Waiting");
                        break;
                    case "port-order-dock":
                        if (containers.length == 0) {status = ShipStatus.DOCKED_FOR_LOADING;}
                        else {status = ShipStatus.DOCKED_FOR_UNLOADING;}
                        AgentUtils.Gui.Send(myAgent, "ship-docked", status.toString() + ":" + containers.length + ":" + arrivalTime + ":" + departureTime);
                        AgentUtils.Gui.Send(myAgent, "console", "Docked");
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed()); // TODO: optimize sleep duration to ensure good messaging
        }
    };

    Behaviour CheckArrival = new TickerBehaviour(this, 1000 / Clock.GetSimulationSpeed())
    {
        @Override
        public void onTick()
        {
            int currentTime = Clock.GetSimulationTime();
            int timeToArrival = getArrivalTime() - currentTime;
            AgentUtils.Gui.Send(myAgent, "console", String.format("Arrival in: %s", timeToArrival));
            if (timeToArrival == 0)
            {
                status = ShipStatus.ARRIVED;
                removeBehaviour(CheckArrival);
                addBehaviour(ArriveAtPort);
            }
        }
    };

    Behaviour ArriveAtPort = new OneShotBehaviour(this)
    {
        @Override
        public void action()
        {
            // notify port that the ship arrived
            AgentUtils.SendMessage(myAgent, portAgent.getName(), ACLMessage.INFORM, "ship-arrived", "arrived");

            AgentUtils.Gui.Send(myAgent, "console", "Arrived and informed port! Containers on board:");

            // print container list
            int i = 0;
            for (Container container : getContainers()) // TODO: store containerAgents instead of containers (get rid of container Class)
            {
                AgentUtils.Gui.Send(myAgent, "console", String.format("%20d - %s", ++i, container.getContainerName()));
            }
        }
    };

    public String getShipName() { return shipName; }
    public int getArrivalTime() { return arrivalTime; }
    public int getDepartureTime() { return departureTime; }
    public Container[] getContainers() { return containers; }
}
