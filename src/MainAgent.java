import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainAgent extends Agent
{
    @Override
    protected void setup()
    {
        AgentContainer ac = getContainerController();

        try {
            AgentController Gui = ac.createNewAgent("GuiAgent", "GuiAgent", null);
            Gui.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
