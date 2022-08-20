import com.terminal.Window;
import jade.core.Agent;
import javax.swing.*;

public class GuiAgent extends Agent
{
    @Override
    protected void setup()
    {
        Object[] PortArgs = getArguments();
        JFrame jFrame = new JFrame("Main Panel");
        jFrame.setContentPane(new Window(Integer.parseInt((String)PortArgs[0]), Integer.parseInt((String)PortArgs[1]), Integer.parseInt((String)PortArgs[2])).mainPanel);
        jFrame.setTitle("Port Terminal Problem");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
