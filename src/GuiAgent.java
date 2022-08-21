import classes.AgentUtils;
import jade.core.Agent;
import javax.swing.*;

public class GuiAgent extends Agent
{
    private int rows;
    private int columns;
    private int stackSize;

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "GuiAgent", "GuiAgent");

        Object[] PortArgs = getArguments();
        rows = Integer.parseInt((String)PortArgs[0]);
        columns = Integer.parseInt((String)PortArgs[1]);
        stackSize = Integer.parseInt((String)PortArgs[2]);

        // Create the GUI Window
        JFrame jFrame = new JFrame("Main Panel");
        jFrame.setContentPane(new Window(rows, columns, stackSize).mainPanel);
        jFrame.setTitle("Port Terminal Simulation");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }


}
