import classes.AgentUtils;
import classes.ShipStatus;
import classes.Utils;
import classes.Utils.Clock;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Queue;

public class ShipAgent extends Agent
{
    private ShipStatus status = ShipStatus.APPROACHING;
    private int arrivalTime;
    private int departureTime;
    private String destination;
    private String shipName;
    private Queue<String> containers;
    private AID portAgent;
    private int timeToArrivalOld = -1;


    @Override
    protected void setup()
    {
        Object[] ShipArgs = getArguments();
        shipName = (String)ShipArgs[0];
        arrivalTime = Integer.parseInt((String)ShipArgs[1]);
        destination = (String)ShipArgs[2];
        containers = (Queue<String>)ShipArgs[3];

        // agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "ShipAgent", shipName);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        // agent gets the portAgent from DF
        portAgent = AgentUtils.searchDF(this, "PortAgent", "PortAgent")[0].getName();


        addBehaviour(NotifyPortOfIncoming);
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
                        AgentUtils.Gui.Send(myAgent, "ship-waiting", status.toString() + ":" + containers.size() + ":" + arrivalTime + ":" + departureTime);
                        AgentUtils.Gui.Send(myAgent, "console", "Waiting");
                        break;
                    case "port-order-dock":
                        if (containers.size() == 0) {status = ShipStatus.DOCKED_FOR_LOADING;}
                        else {status = ShipStatus.DOCKED_FOR_UNLOADING;}
                        AgentUtils.Gui.Send(myAgent, "ship-docked", status.toString() + ":" + containers.size() + ":" + arrivalTime + ":" + departureTime);
                        AgentUtils.Gui.Send(myAgent, "console", "Docked");
                        break;
                    case "unloader-request-container":
                        sendNextContainerToUnloader(msg);
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    Behaviour NotifyPortOfIncoming = new OneShotBehaviour(this)
    {
        @Override
        public void action()
        {
            // notify port that the ship is on its way
            AgentUtils.SendMessage(myAgent, portAgent, ACLMessage.INFORM, "ship-incoming", destination + ":" + arrivalTime);
        }
    };

    Behaviour CheckArrival = new TickerBehaviour(this, 1000 / Clock.GetSimulationSpeed())
    {
        @Override
        public void onTick()
        {
            int currentTime = Clock.GetSimulationTime();
            int timeToArrival = getArrivalTime() - currentTime;

            if (timeToArrival != timeToArrivalOld)
            {
                timeToArrivalOld = timeToArrival;
                AgentUtils.Gui.Send(myAgent, "console", String.format("Arrival in: %s", timeToArrival));
            }

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
            AgentUtils.SendMessage(myAgent, portAgent, ACLMessage.INFORM, "ship-arrived", "arrived");
        }
    };

    private void sendNextContainerToUnloader(ACLMessage msg)
    {
        String nextContainer;
        if (containers.size() == 0) { nextContainer = "All containers unloaded"; } // all unloaded
        else { nextContainer = containers.remove(); }

        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.INFORM);
        response.setOntology("ship-next-container");
        response.setContent(nextContainer);
        send(response);
    }




    public String getShipName() { return shipName; }
    public int getArrivalTime() { return arrivalTime; }
    public int getDepartureTime() { return departureTime; }
    public Queue<String> getContainers() { return containers; }

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
