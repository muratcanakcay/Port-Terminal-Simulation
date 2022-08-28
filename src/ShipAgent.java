import classes.AgentUtils;
import classes.Container;
import classes.Ship;
import classes.Utils.Clock;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

public class ShipAgent extends Agent
{
    Ship ship;
    DFAgentDescription portAgent;

    @Override
    protected void setup()
    {
        Object[] ShipArgs = getArguments();
        String shipName = (String)ShipArgs[0];
        int arrivalTime = Integer.parseInt((String)ShipArgs[1]);
        int departureTime = Integer.parseInt((String)ShipArgs[2]);
        Container[] containers = (Container[])ShipArgs[3];

        ship = new Ship(shipName, arrivalTime, departureTime, containers);

        // agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "ShipAgent", shipName);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        // agent gets the portAgent from DF
        portAgent = AgentUtils.searchDF(this, "portAgent", "portAgent")[0];

        addBehaviour(checkArrival);
    }

    Behaviour checkArrival = new TickerBehaviour(this, 1000 / Clock.GetSimulationSpeed())
    {
        @Override
        public void onTick()
        {
            int currentTime = Clock.GetSimulationTime();
            int timeToArrival = ship.getArrivalTime() - currentTime;
            AgentUtils.Gui.Send(myAgent, "console", String.format("Arrival in: %s", timeToArrival));
            if (timeToArrival == 0)
            {
                removeBehaviour(checkArrival);
                addBehaviour(arriveAtPort);
            }
        }
    };

    Behaviour arriveAtPort = new OneShotBehaviour(this)
    {
        @Override
        public void action()
        {
            // notify port that the ship arrived
            AgentUtils.SendMessage(myAgent, portAgent.getName(), ACLMessage.INFORM, "ship-arrived", ship.getName() + "arrived");

            AgentUtils.Gui.Send(myAgent, "console", "Arrived at port! Containers on board:");

            // print container list
            int i = 0;
            for (Container container : ship.getContainers())
            {
                AgentUtils.Gui.Send(myAgent, "console", String.format("%20d - %s", ++i, container.getName()));
            }
        }
    };

}
