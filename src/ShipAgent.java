import classes.AgentUtils;
import classes.Container;
import classes.Ship;
import classes.Utils.Clock;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;

public class ShipAgent extends Agent
{
    Ship ship;

    @Override
    protected void setup()
    {
        Object[] ShipArgs = getArguments();
        String shipName = "Ship" + ShipArgs[0];
        int arrivalTime = Integer.parseInt((String)ShipArgs[1]);
        int departureTime = Integer.parseInt((String)ShipArgs[2]);
        Container[] containers = (Container[])ShipArgs[3];

        ship = new Ship(shipName, arrivalTime, departureTime, containers);

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "ShipAgent", shipName);

        System.out.printf("[%s] Agent is registered to DF.\n", ship.getName());

        addBehaviour(checkArrival);
    }

    Behaviour checkArrival = new TickerBehaviour(this, 1000/ Clock.GetSimulationSpeed())
    {
        @Override
        public void onTick()
        {
            int currentTime = Clock.GetSimulationTime();
            int timeToArrival = ship.getArrivalTime() - currentTime;
            System.out.printf("[%s] Simulation Time: %s  Arrival: %s\n", ship.getName(), currentTime, timeToArrival);
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
            System.out.printf("[%s] Arrived at port! Containers on board:\n", ship.getName());


            // print container list
            for (Container container : ship.getContainers())
            {
                System.out.println(container.getName());
            }
        }
    };

}
