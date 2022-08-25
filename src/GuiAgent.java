import classes.AgentUtils;
import classes.Utils;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import javax.swing.*;

public class GuiAgent extends Agent
{
    private JFrame mainFrame;
    private guiWindow guiWindow;
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
        guiWindow = new guiWindow(rows, columns, stackSize);
        mainFrame = new JFrame("Main Frame");
        mainFrame.setContentPane(guiWindow.mainPanel);
        mainFrame.setTitle("Port Terminal Simulation");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);

        consoleLog("[Gui] Started.");

        addBehaviour(ReceiveMessages);
    }

    Behaviour ReceiveMessages = new CyclicBehaviour(this) {
        @Override
        public void action()
        {
            ACLMessage msg = receive();

            if (msg != null)
            {
                switch(msg.getOntology())
                {
                    case "console":
                        consoleLog(msg.getContent());

//                        ACLMessage response = msg.createReply();
//                        response.setPerformative(ACLMessage.INFORM);
//                        response.setContent("Today it's raining.");
//                        System.out.println(getAID().getName() + " is sending response message!");
//                        send(response);
                        break;
                }
            }

            try {
                Thread.sleep(10 / Utils.Clock.GetSimulationSpeed()); // TODO: optimize sleep duration to ensure good messaging
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };


    private void consoleLog(String msg)
    {
        // displays simulation time and text in the console area of Gui
        // TODO: insert sending agent's name and align instead of receiving the name in the msg
        guiWindow.getConsole().append("[" + Utils.Clock.GetSimulationTime() + "] " + msg + "\n");
    }
}
