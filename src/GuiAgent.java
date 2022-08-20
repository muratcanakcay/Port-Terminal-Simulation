import com.terminal.Window;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import javax.swing.*;

public class GuiAgent extends Agent
{
    int rows;
    int columns;
    int stackSize;

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("Gui");
        sd.setName("GuiAgent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        Object[] PortArgs = getArguments();
        rows = Integer.parseInt((String)PortArgs[0]);
        columns = Integer.parseInt((String)PortArgs[1]);
        stackSize = Integer.parseInt((String)PortArgs[2]);

        JFrame jFrame = new JFrame("Main Panel");
        jFrame.setContentPane(new Window(rows, columns, stackSize).mainPanel);
        jFrame.setTitle("Port Terminal Simulation");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.pack();
        jFrame.setVisible(true);
    }
}
