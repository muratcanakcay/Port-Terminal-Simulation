import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainAgent extends Agent
{
    Object[] PortArgs = {"3", "4", "5"};   // Port storage grid rows, columns, stackSize

    @Override
    protected void setup()
    {
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
