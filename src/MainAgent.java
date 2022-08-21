import classes.AgentUtils;
import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainAgent extends Agent
{
    Object[] PortArgs = {"3", "4", "5"};   // Container storage stats: rows, columns, stackSize

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "MainAgent", "MainAgent");

        AgentContainer ac = getContainerController();

        try
        {
            AgentController Gui = ac.createNewAgent("GuiAgent", "GuiAgent", PortArgs);
            AgentController Port = ac.createNewAgent("PortAgent", "PortAgent", PortArgs);
            Gui.start();
            Port.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
