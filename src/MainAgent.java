import classes.AgentUtils;
import classes.Utils.Clock;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

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
            AgentController Gui = ac.createNewAgent("GuiAgent", "GuiAgent", PortArgs);
            AgentController Port = ac.createNewAgent("PortAgent", "PortAgent", PortArgs);
            AgentController Ship = ac.createNewAgent("Ship001", "ShipAgent", new Object[]{"001", "5", "10"});
            Gui.start();
            Port.start();
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
            System.out.println("[MainAgent] Simulation Time: " + Clock.GetSimulationTime());
        }
    };
}
