import classes.AgentUtils;
import classes.Utils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.*;

public class PortAgent extends Agent
{
    private String unloaderType = "BasicUnloaderAgent";
    private UnloaderFactory unloaderFactory;
    private AgentContainer ac;
    private int rows;
    private int columns;
    private int stackSize;
    private int noOfCranes;
    private int dockSize; // no of ships that can dock simultaneously

    Queue<AID> waitingShips = new PriorityQueue<AID>();
    List<AID> dockedShips = new ArrayList<jade.core.AID>();
    List<String> incomingShips = new ArrayList<String>();


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

        ac = getContainerController();
        unloaderFactory = new UnloaderFactory(ac, unloaderType);
        createCellAgents();
        createCraneAgents();

        // test unloaderAgent (since there's no Ship002, it will fail and be indicated in red in the console )
        unloaderFactory.createUnloaderAgentFor("Ship002");
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
                    case "ship-incoming":
                        handleIncomingShip(msg);
                        break;

                    case "ship-arrived":
                        handleArrivedShip(msg.getSender());
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void handleIncomingShip(ACLMessage msg)
    {
        String[] shipInfo = msg.getContent().split(":");
        incomingShips.add(msg.getSender().getLocalName() + ":" + shipInfo[0] + ":" + shipInfo[1]);
        updateGuiIncomingShips();
    }

    private void updateGuiIncomingShips()
    {
        String incomingShipsList = "";

        for (String shipInfo : incomingShips)
        {
            String[] shipInfoParts = shipInfo.split(":");
            incomingShipsList += "Ship Name: " + shipInfoParts[0] + " ----- Destination: " + shipInfoParts[1] + " ----- ArrivalTime: " + shipInfoParts[2] + "\n";
        }

        AgentUtils.Gui.Send(this, "port-incoming-ships", incomingShipsList);
    }

    private void handleArrivedShip(AID shipAgent)
    {
        removeFromIncomingShips(shipAgent.getLocalName());

        if (dockedShips.size() == dockSize)
        {
            waitingShips.add(shipAgent);
            AgentUtils.SendMessage(this, shipAgent, ACLMessage.INFORM, "port-order-wait", "wait");
        }
        else
        {
            dockedShips.add(shipAgent);
            AgentUtils.SendMessage(this, shipAgent, ACLMessage.INFORM, "port-order-dock", "dock");

            //create unloader for the docked ship
            unloaderFactory.createUnloaderAgentFor(shipAgent.getLocalName());
        }
    }

    private void removeFromIncomingShips(String shipName)
    {
        int i = 0;
        for (String shipInfo : incomingShips)
        {
            String[] shipInfoParts = shipInfo.split(":");
            if (Objects.equals(shipInfoParts[0], shipName)) break;
            ++i;
        }

        incomingShips.remove(i);
        updateGuiIncomingShips();
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getStackSize() { return stackSize; }
    public int getNoOfCranes() { return noOfCranes; }
    public int getNoOfWaitingShips() { return waitingShips.size(); }

    private void createCraneAgents()
    {
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
    }

    private void createCellAgents()
    {
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
