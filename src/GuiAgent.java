import com.terminal.MainFrame;
import jade.core.Agent;
import javax.swing.*;

public class GuiAgent extends Agent
{
    @Override
    protected void setup()
    {
        JFrame jFrame = new JFrame("Main Panel");
        jFrame.setContentPane(new MainFrame().mainPanel);
        jFrame.setTitle("Port Terminal Problem");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
