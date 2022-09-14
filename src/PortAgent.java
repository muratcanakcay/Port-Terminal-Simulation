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
    private String loaderType = "AdvancedLoaderAgent";
    private UnloaderFactory unloaderFactory;
    private LoaderFactory loaderFactory;
    private AgentContainer ac;
    private int rows;
    private int columns;
    private int stackSize;
    private int noOfCranes;
    private int dockSize; // no of ships that can dock simultaneously

    private final Queue<AID> waitingShips = new PriorityQueue<AID>();
    private final List<AID> dockedShips = new ArrayList<jade.core.AID>();
    private final List<String> incomingShips = new ArrayList<String>();


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
        AgentUtils.Gui.Send(this, "console", "Port created with Rows: " + rows + " Columns: " + columns + " StackSize: " + stackSize + " noOfCranes: " + noOfCranes);

        ac = getContainerController();
        unloaderFactory = new UnloaderFactory(ac, unloaderType, columns);
        loaderFactory = new LoaderFactory(ac, loaderType, columns);
        createCellAgents();
        createCraneAgents();

        // test unloaderAgent (since there's no Ship002, it will fail and be indicated in red in the console )
        unloaderFactory.createUnloaderAgentFor("Ship002");
        // test loaderAgent (since there's no Ship003, it will fail and be indicated in red in the console )
        loaderFactory.createLoaderAgentFor("Ship003", "X");
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
                        handleArrivedShip(msg);
                        break;
                    case "ship-departed":
                        AID shipAgent = msg.getSender();
                        dockedShips.remove(shipAgent);
                        break;
                    case "unloader-request-shipETA":
                        sendShipETAtoUnloader(msg);
                        break;
                    case "unloader-unloading-finished":
                    case "loader-loading-finished":
                        String shipName = msg.getContent().split(":")[1];
                        orderShipToDepart(shipName);
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void orderShipToDepart(String shipName)
    {
        AID shipAgent = AgentUtils.searchDFbyName(this, shipName)[0].getName();
        AgentUtils.SendMessage(this, shipAgent, ACLMessage.REQUEST, "port-order-depart", "Depart");
    }

    private void sendShipETAtoUnloader(ACLMessage msg)
    {
        String destination = msg.getContent();
        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.INFORM);
        response.setOntology("port-shipETA");

        String shipETA = "-1";
        for (String shipInfo : incomingShips)
        {
            String[] shipInfoParts = shipInfo.split(":");
            if (Objects.equals(shipInfoParts[1], destination)) { shipETA = shipInfoParts[2];}
        }

        // TODO: also implement check for waiting ships otherwise waiting ships will not be taken into account and it will be like no ship is going to the destination

        response.setContent(shipETA);
        send(response);
    }

    private void handleIncomingShip(ACLMessage msg)
    {
        String[] shipInfo = msg.getContent().split(":");
        incomingShips.add(msg.getSender().getLocalName() + ":" + shipInfo[0] + ":" + shipInfo[1]);
        incomingShips.sort(new SortByETA());
        updateGuiIncomingShips();
    }

    static class SortByETA implements Comparator<String>
    {
        public int compare(String a, String b)
        {
            return Integer.parseInt(a.split(":")[2]) - Integer.parseInt(b.split(":")[2]);
        }
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

    private void handleArrivedShip(ACLMessage msg)
    {
        AID shipAgent = msg.getSender();
        String[] shipData = msg.getContent().split(":");
        int containerCount = Integer.parseInt(shipData[0]);
        String destination = shipData[1];

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

            //create unloader for the docked empty ship
            if (containerCount > 0) { unloaderFactory.createUnloaderAgentFor(shipAgent.getLocalName()); }
            else { loaderFactory.createLoaderAgentFor(shipAgent.getLocalName(), destination); }
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

    private void createCraneAgents()
    {
        try
        {
            for (int i = 0; i < noOfCranes; ++i)
            {
                Object[] CraneArgs = {"Crane0" + (i+1)};
                AgentController Cell = ac.createNewAgent("Crane0" + (i+1), "CraneAgent", CraneArgs);
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
                    Object[] CellArgs = {String.valueOf(r), String.valueOf(c), String.valueOf(stackSize)};
                    AgentController Cell = ac.createNewAgent("Cell:" + r + ":" + c, "CellAgent", CellArgs);
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
