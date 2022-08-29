import classes.AgentUtils;
import classes.Container;
import classes.Utils.Clock;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import static java.util.UUID.randomUUID;

public class MainAgent extends Agent
{
    private final int simulationSpeed = 1;
    Object[] PortArgs = {"3", "4", "5", "2", "1"};   // rows, columns, stackSize, noOfCranes, dockSize

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "MainAgent", "MainAgent");

        // start the clock
        Clock clock = new Clock(simulationSpeed);

        AgentContainer ac = getContainerController();

        try
        {
            // start GuiAgent
            AgentController Gui = ac.createNewAgent("GuiAgent", "GuiAgent", PortArgs);
            Gui.start();

            // wait for Gui to start // TODO: may need more time to start!
            Thread.sleep(3000);

            // initialize AgentUtils.Gui for global use
            AgentUtils.Gui gui = new AgentUtils.Gui(this);

            // start PortAgent
            AgentController Port = ac.createNewAgent("PortAgent", "PortAgent", PortArgs);
            Port.start();

            // create 3 sample containers to add to ship
            Container[] containers = new Container[3];
            for (int i = 0; i < 3; i++)
            {
                String containerName = String.valueOf(randomUUID());
                Container container = new Container(containerName, "Neverland");
                containers[i] = container;
                AgentController Container = ac.createNewAgent(containerName, "ContainerAgent", new Object[]{container});
                Container.start();
            }

            // TODO: implement a method to add a new ship with incremental ship number
            int shipNumber = 58;
            String shipName = "Ship";
            shipName += shipNumber < 10 ? "00" : shipNumber < 100 ? "0" : "";
            shipName += shipNumber;
            AgentController Ship = ac.createNewAgent(shipName, "ShipAgent", new Object[]{shipName, "5", "10", containers});
            Ship.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        addBehaviour(runClock);
    }

    Behaviour runClock = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            Clock.tick();
            AgentUtils.Gui.Send(myAgent, "clock-tick", String.valueOf(Clock.GetSimulationTime()));
            block(1000/Clock.GetSimulationSpeed());
        }
    };
}
