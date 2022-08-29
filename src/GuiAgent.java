import classes.AgentUtils;
import classes.Utils;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.Objects;

public class GuiAgent extends Agent
{
    private guiWindow guiWindow;
    private int dockSize;

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "GuiAgent", "GuiAgent");

        Object[] PortArgs = getArguments();
        int rows = Integer.parseInt((String) PortArgs[0]);
        int columns = Integer.parseInt((String) PortArgs[1]);
        int stackSize = Integer.parseInt((String) PortArgs[2]);
        int noOfCranes = Integer.parseInt((String) PortArgs[3]);
        dockSize = Integer.parseInt((String)PortArgs[4]);

        // Create the GUI Window
        guiWindow = new guiWindow(rows, columns, stackSize, noOfCranes, dockSize);
        JFrame mainFrame = new JFrame("Main Frame");
        mainFrame.setContentPane(guiWindow.mainPanel);
        mainFrame.setTitle("Port Terminal Simulation");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);

        consoleLog(getAID(), "Started.", Color.BLACK, Color.WHITE);

        addBehaviour(ReceiveMessages);
    }


    Behaviour ReceiveMessages = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            ACLMessage msg = receive();

            if (msg != null)
            {
                switch(msg.getOntology())
                {
                    case "console":
                        consoleLog(msg.getSender(), msg.getContent(), Color.BLACK, Color.WHITE);

//                        ACLMessage response = msg.createReply();
//                        response.setPerformative(ACLMessage.INFORM);
//                        response.setContent("Today it's raining.");
//                        System.out.println(getAID().getName() + " is sending response message!");
//                        send(response);
                        break;
                    case "console-error":
                        consoleLog(msg.getSender(), msg.getContent(), Color.RED, Color.WHITE);
                        break;
                    case "ship-docked":
                        setShipDocked(msg);
                        break;
                    case "clock-tick":
                        ((JTextField)((JPanel)guiWindow.getSimulationTimePanel().getComponent(0)).getComponent(0)).setText(msg.getContent());
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void setShipDocked(ACLMessage msg)
    {
        // TODO: must update waiting list from portAgent (after implementing approaching/waiting ships display)

        Component[] dockGridComponents = guiWindow.getDockGrid().getComponents();

        // find empty row in dockGrid and fill it with ship info from msg
        for (int i = 1; i < dockSize + 1; ++i) // skip first row - it's for headers
        {
            if (Objects.equals(((JTextField)dockGridComponents[5*i]).getText(), ""))
            {
                ((JTextField)dockGridComponents[5*i]).setText(msg.getSender().getLocalName());

                String[] shipInfo = msg.getContent().split(":", -1);

                ((JTextField)dockGridComponents[5*i + 1]).setText(shipInfo[0]);
                ((JTextField)dockGridComponents[5*i + 2]).setText(shipInfo[1]);
                ((JTextField)dockGridComponents[5*i + 3]).setText(shipInfo[2]);
                ((JTextField)dockGridComponents[5*i + 4]).setText(shipInfo[3]);

            }
        }
    }


    private void consoleLog(jade.core.AID sender, String msg, Color textColor, Color highlightColor)
    {
        // displays simulation time and text in the console area of Gui
        Document doc = guiWindow.getConsole().getStyledDocument();

        try
        {
            // simulation time
            SimpleAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setBold(attributeSet, true);
            doc.insertString(doc.getLength(), "[" + Utils.Clock.GetSimulationTime() + "] ", attributeSet);

            // sender
            attributeSet = new SimpleAttributeSet();
            StyleConstants.setBold(attributeSet, true);
            StyleConstants.setForeground(attributeSet, Color.blue);
            doc.insertString(doc.getLength(), "[" + sender.getLocalName() + "] ", attributeSet);

            // message
            attributeSet = new SimpleAttributeSet();
            StyleConstants.setForeground(attributeSet, textColor);
            StyleConstants.setBackground(attributeSet, highlightColor);
            doc.insertString(doc.getLength(), msg + "\n", attributeSet);
        }
        catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}
