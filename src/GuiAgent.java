import classes.AgentUtils;
import classes.Utils;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class GuiAgent extends Agent
{
    private DFAgentDescription mainAgent;
    private guiWindow guiWindow;
    private int dockSize;
    private int noOfCranes;
    private int stackSize;
    private int columns;
    private boolean clockRunning = true;

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "GuiAgent", "GuiAgent");

        // get mainAgent from DF to be able to send button clicks to it
        mainAgent = AgentUtils.searchDFbyName(this, "MainAgent")[0];

        Object[] PortArgs = getArguments();
        int rows = Integer.parseInt((String) PortArgs[0]);
        columns = Integer.parseInt((String) PortArgs[1]);
        stackSize = Integer.parseInt((String) PortArgs[2]);
        noOfCranes = Integer.parseInt((String) PortArgs[3]);
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

        initializePauseButton(this);

        addBehaviour(ReceiveMessages);
    }

    private void initializePauseButton(Agent guiAgent)
    {
        Button pauseButton = guiWindow.getPauseButton();

        pauseButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                AgentUtils.SendMessage(guiAgent, mainAgent.getName(), ACLMessage.INFORM, "play-pause", "play-pause");

                if (clockRunning) { pauseButton.setLabel("Play"); }
                else {pauseButton.setLabel("Pause");}

                clockRunning = !clockRunning;
            }
        });
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
                    case "port-incoming-ships":
                        setIncomingShips(msg.getContent());
                        break;
                    case "crane-moving-container":
                        String[] craneInfo = msg.getContent().split("_");
                        updateCrane(msg.getSender().getLocalName(), craneInfo[0], craneInfo[1], craneInfo[2], craneInfo[3]);
                        break;
                    case "crane-unloaded-ship":
                        updateCrane(msg.getSender().getLocalName(), "", "", "", "IDLE");
                        updateDockedShip(msg.getContent(), false);
                        break;
                    case "loader-loaded-ship":
                        updateDockedShip(msg.getContent(), true);
                        break;
                    case "cell-received-container":
                        String[] cellNameParts = msg.getSender().getLocalName().split(":");
                        int cellRow = Integer.parseInt(cellNameParts[1]);
                        int cellColumn = Integer.parseInt(cellNameParts[2]);
                        String containerData = msg.getContent();

                        updateCell(cellRow, cellColumn, containerData);
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void updateCell(int cellRow, int cellColumn, String containerData)
    {
        String[] containerDataParts = containerData.split(":");
        int stackPosition = stackSize - 1 - Integer.parseInt(containerDataParts[0]);
        String containerName = containerDataParts[1];
        String destination = containerDataParts[2];
        String pickupTime  = containerDataParts[3];

        Component[] cellComponents = guiWindow.getCellGrid().getComponents();
        JPanel cellPanel = ((JPanel)cellComponents[cellRow * columns + cellColumn]);

        Component[] stackComponents = cellPanel.getComponents();
        JTextField stackTextField = ((JTextField)stackComponents[stackPosition]);

        // TODO: would be good to format this so everything lines up in GUI
        stackTextField.setText(containerName.substring(0, 6) + "... -- Dest: " + destination + " -- shipETA: " + pickupTime);
    }

    private void updateCrane(String craneName, String containerName, String from, String to, String craneStatus)
    {
        Component[] craneGridComponents = guiWindow.getCraneGrid().getComponents();

        // find crane's row in dockGrid and update info
        for (int i = 1; i < noOfCranes + 1; ++i) // skip first row - it's for headers
        {
            if (Objects.equals(((JTextField)craneGridComponents[5*i]).getText(), craneName))
            {
                ((JTextField)craneGridComponents[5*i + 1]).setText(craneStatus);            // status
                ((JTextField)craneGridComponents[5*i + 2]).setText(containerName);          // container
                ((JTextField)craneGridComponents[5*i + 3]).setText(from);                   // from
                ((JTextField)craneGridComponents[5*i + 4]).setText(to);                     // to
                break;
            }
        }
    }

    private void updateDockedShip(String shipName, boolean increaseContainerCount)
    {
        Component[] dockGridComponents = guiWindow.getDockGrid().getComponents();

        // find ship's row in dockGrid and update containers
        for (int i = 1; i < dockSize + 1; ++i) // skip first row - it's for headers
        {
            if (Objects.equals(((JTextField)dockGridComponents[5*i]).getText(), shipName))
            {
                int containerCount = Integer.parseInt(((JTextField)dockGridComponents[5*i + 2]).getText());
                containerCount += increaseContainerCount ? 1 : -1;

                ((JTextField)dockGridComponents[5*i + 2]).setText(String.valueOf(containerCount));
                break;
            }
        }
    }

    private void setIncomingShips(String content)
    {
        JTextPane incomingShips = guiWindow.getIncomingShips();
        incomingShips.setText(content);
    }

    private void setShipDocked(ACLMessage msg)
    {
        // TODO: must update waiting list from portAgent (after implementing approaching/waiting ships display)

        Component[] dockGridComponents = guiWindow.getDockGrid().getComponents();

        // find empty row in dockGrid and fill it with ship info from msg
        for (int i = 1; i < dockSize + 1; ++i) // skip first row - it's for headers
        {
            if (Objects.equals(((JTextField)dockGridComponents[5*i]).getText(), ""))        // check empty row
            {
                String[] shipInfo = msg.getContent().split(":", -1);

                ((JTextField)dockGridComponents[5*i]).setText(msg.getSender().getLocalName());  // ship name
                ((JTextField)dockGridComponents[5*i + 1]).setText(shipInfo[0]);                 // status
                ((JTextField)dockGridComponents[5*i + 2]).setText(shipInfo[1]);                 // no of containers
                ((JTextField)dockGridComponents[5*i + 3]).setText(shipInfo[2]);                 // arrival time
                ((JTextField)dockGridComponents[5*i + 4]).setText(shipInfo[3]);                 // destination

                break;
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

    @Override
    protected void takeDown()
    {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println(getAID().getName() + " terminated.");
    }
}
