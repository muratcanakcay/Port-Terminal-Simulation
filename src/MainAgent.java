import classes.AgentUtils;
import classes.Container;
import classes.Utils.Clock;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import static java.util.UUID.randomUUID;

public class MainAgent extends Agent
{
    private int simulationSpeed = 1;
    Object[] PortArgs = {"3", "4", "5"};   // Container storage stats: rows, columns, stackSize

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
            AgentController Ship = ac.createNewAgent("Ship001", "ShipAgent", new Object[]{"001", "5", "10", containers});
            Ship.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        addBehaviour(runClock);
    }

    Behaviour runClock = new TickerBehaviour(this, 1000/simulationSpeed)
    {
        @Override
        public void onTick()
        {
            Clock.tick();
            // System.out.println("[MainAgent] Simulation Time: " + Clock.GetSimulationTime());
        }
    };
}
