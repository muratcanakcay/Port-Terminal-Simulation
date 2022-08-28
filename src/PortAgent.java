import classes.AgentUtils;
import classes.Utils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class PortAgent extends Agent
{
    private int rows;
    private int columns;
    private int stackSize;
    private int noOfCranes;
    private int dockSize; // no of ships that can dock simultaneously

    Queue<AID> waitingShips = new PriorityQueue<AID>();
    List<jade.core.AID> dockedShips = new ArrayList<jade.core.AID>();


    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "PortAgent", "PortAgent");

        // get arguments
        Object[] PortArgs = getArguments();
        rows = Integer.parseInt((String)PortArgs[0]);
        columns = Integer.parseInt((String)PortArgs[1]);
        stackSize = Integer.parseInt((String)PortArgs[2]);
        noOfCranes = Integer.parseInt((String)PortArgs[3]);
        dockSize = Integer.parseInt((String)PortArgs[4]);

        // start listening for messages
        addBehaviour(ReceiveMessages);

        // informative console log
        AgentUtils.Gui.Send(this, "console", "Port created with Rows: " + getRows() + " Columns: " + getColumns() + " StackSize: " + getStackSize() + " noOfCranes: " + getNoOfCranes());

        AgentContainer ac = getContainerController();

        // create the cell agents of the container storage
        try
        {
            for (int r = 0; r < rows; ++r)
            {
                for (int c = 0; c < columns; ++c)
                {
                    Object[] CellArgs = {String.valueOf(r), String.valueOf(c), String.valueOf(getStackSize())};
                    AgentController Cell = ac.createNewAgent("Cell" + r + ":" + c, "CellAgent", CellArgs);
                    Cell.start();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // create the craneAgents
        try
        {
            for (int i = 0; i < noOfCranes; ++i)
            {
                Object[] CraneArgs = {"Crane0" + i};
                AgentController Cell = ac.createNewAgent("Crane0" + i, "CraneAgent", CraneArgs);
                Cell.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // create a sample unloaderAgent TODO: delete later
        try
        {
            String shipName = "Ship002";
            String unloaderName = "UnloaderFor" + shipName;
            Object[] UnloaderArgs = {unloaderName, shipName};
            AgentController Unloader = ac.createNewAgent(unloaderName, "UnloaderAgent", UnloaderArgs);
            Unloader.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    Behaviour ReceiveMessages = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            ACLMessage msg = receive();

            if (msg != null)
            {
                AgentUtils.Gui.Send(myAgent, "console", "Received message from: " +  msg.getSender().getLocalName() + " : " + msg.getContent());

                switch(msg.getOntology())
                {
                    case "ship-arrived":
                        handleArrivedShip(msg.getSender());
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

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getStackSize() { return stackSize; }
    public int getNoOfCranes() { return noOfCranes; }
    public int getNoOfWaitingShips() { return waitingShips.size(); }

    private void handleArrivedShip(AID shipAgent)
    {
        if (dockedShips.size() == dockSize)
        {
            waitingShips.add(shipAgent);
            AgentUtils.SendMessage(this, shipAgent, ACLMessage.INFORM, "port-order-wait", "wait");
        }
        else
        {
            dockedShips.add(shipAgent);
            AgentUtils.SendMessage(this, shipAgent, ACLMessage.INFORM, "port-order-dock", "dock");
            AgentUtils.Gui.Send(this, "console", "Ordered " + shipAgent.getLocalName() + " to dock");

        }
    }
}
