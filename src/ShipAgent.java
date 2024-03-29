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

import java.util.Objects;
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
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF. Carrying " + containers.size() + " containers to destination " + destination);

        // agent gets the portAgent from DF
        portAgent = AgentUtils.searchDFbyName(this, "PortAgent")[0].getName();

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
                        AgentUtils.Gui.Send(myAgent, "ship-waiting", status.toString() + ":" + containers.size() + ":" + arrivalTime + ":" + destination);
                        AgentUtils.Gui.Send(myAgent, "console", "Waiting");
                        break;
                    case "port-order-dock":
                        if (containers.size() == 0) {status = ShipStatus.DOCKED_FOR_LOADING;}
                        else {status = ShipStatus.DOCKED_FOR_UNLOADING;}
                        AgentUtils.Gui.Send(myAgent, "ship-docked", status.toString() + ":" + containers.size() + ":" + arrivalTime + ":" + destination);
                        AgentUtils.Gui.Send(myAgent, "console", "Docked with " + containers.size() + " containers");
                        break;
                    case "port-order-depart":
                        status = ShipStatus.DEPARTING;
                        AgentUtils.Gui.Send(myAgent, "ship-departing", status.toString());

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        status = ShipStatus.DEPARTED;
                        AgentUtils.SendMessage(myAgent, portAgent, ACLMessage.INFORM, "ship-departed", "Departed");
                        AgentUtils.Gui.Send(myAgent, "ship-departed", status.toString());
                        AgentUtils.Gui.Send(myAgent, "console", "Departed with " + containers.size() + " containers" + (Objects.equals(destination, "mainPort") ? "" : (" to destination " + destination)));
                        break;
                    case "unloader-request-container":
                        sendNextContainerToUnloader(msg);
                        break;
                    case "crane-put-container":
                        String containerData = msg.getContent();
                        receiveContainer(containerData);
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void receiveContainer(String containerData)
    {
        String containerName = containerData.split(":")[0];
        containers.add(containerName);
        AgentUtils.Gui.Send(this, "console", "Received container: " + containerData);
    }

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
            int timeToArrival = arrivalTime - currentTime;

            if (timeToArrival != timeToArrivalOld)
            {
                timeToArrivalOld = timeToArrival;
                //AgentUtils.Gui.Send(myAgent, "console", String.format("Arrival in: %s", timeToArrival));
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
            AgentUtils.SendMessage(myAgent, portAgent, ACLMessage.INFORM, "ship-arrived", containers.size() + ":" + destination);
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
