import classes.AgentUtils;
import classes.Utils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeInitiator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class UnloaderAgent extends Agent
{
    private String name;
    protected int columns;
    private String currentContainerName;
    protected String currentCellName;
    protected String currentDestination;
    protected int currentShipETA;
    protected AID shipAgent;
    private AID portAgent;
    private final List<AID> cellAgents = new ArrayList<AID>();
    private final List<AID> craneAgents = new ArrayList<AID>();
    protected final List<String> availableCells = new ArrayList<>();
    private final List<AID> availableCranes = new ArrayList<>();



    @Override
    protected void setup() {
        Object[] UnloaderArgs = getArguments();
        name = (String)UnloaderArgs[0];
        String shipName = (String)UnloaderArgs[1];
        columns = (int) UnloaderArgs[2];

        DFAgentDescription[] ships = AgentUtils.searchDFbyName(this, shipName);

        // agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "UnloaderAgent", name);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        if (ships.length != 1)
        {
            AgentUtils.Gui.Send(this, "console-error", "There's " + ships.length + " ship(s) with the name: " + shipName);
            doDelete();
            return;
        }

        // agent gets the shipAgent from DF
        shipAgent = ships[0].getName();
        // agent gets the portAgent from DF
        portAgent = AgentUtils.searchDFbyName(this, "PortAgent")[0].getName();
        // agent gets the cellAgents from DF
        DFAgentDescription[] cellAgentDescriptions = AgentUtils.searchDFbyType(this, "CellAgent");
        for (DFAgentDescription cellAgentDescription : cellAgentDescriptions) { cellAgents.add(cellAgentDescription.getName()); }
        // agent gets the craneAgents from DF
        DFAgentDescription[] craneAgentDescriptions = AgentUtils.searchDFbyType(this, "CraneAgent");
        for (DFAgentDescription craneAgentDescription : craneAgentDescriptions) { craneAgents.add(craneAgentDescription.getName()); }

        addBehaviour(ReceiveMessages);
        addBehaviour(RequestContainerFromShip);
    }

    Behaviour ReceiveMessages = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);

            if (msg != null)
            {
                AgentUtils.Gui.Send(myAgent, "console", "Received message from: " +  msg.getSender().getLocalName() + " : " + msg.getContent());

                switch(msg.getOntology())
                {
                    case "ship-next-container":
                        currentContainerName = msg.getContent();
                        if (Objects.equals(currentContainerName, "All containers unloaded")) { concludeUnloadingProcedure(); }
                        else { requestContainerDestination(msg.getContent()); }
                        break;
                    case "container-destination":
                        currentDestination = msg.getContent();
                        requestETAofShipToDestination(currentDestination);
                        break;
                    case "port-shipETA":
                        currentShipETA = Integer.parseInt(msg.getContent());
                        if (currentShipETA < 0) { AgentUtils.Gui.Send(myAgent, "console-error", "There's no ship that will take " + currentContainerName + " to destination " + currentDestination); }
                        sendCFPtoCells(msg.getContent());
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void concludeUnloadingProcedure()
    {
        AgentUtils.SendMessage(this, portAgent, ACLMessage.INFORM, "unloader-unloading-finished", "Unloading of :" + shipAgent.getLocalName() + ": is finished");
        doDelete();
    }

    Behaviour RequestContainerFromShip = new OneShotBehaviour(this)
    {
        @Override
        public void action()
        {
            AgentUtils.SendMessage(myAgent, shipAgent, ACLMessage.QUERY_IF, "unloader-request-container", "Requesting next container to unload");
        }
    };

    private void sendCFPtoCells(String shipETA)
    {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

        for (AID cellAgent : cellAgents)
        {
            cfp.addReceiver(cellAgent);
        }

        cfp.setContent("");
        cfp.setPerformative(ACLMessage.QUERY_IF);

        addBehaviour(new ProposeInitiator(this, cfp)
        {
            protected void handleAcceptProposal(ACLMessage accept_proposal)
            {
                String cellContents = accept_proposal.getContent();
                String availableCell = accept_proposal.getSender().getName() + "_" + cellContents;
                availableCells.add(availableCell);
                AgentUtils.Gui.Send(myAgent, "console-error", "availableCell--> " + availableCell );
            };

            protected void handleAllResponses(java.util.Vector responses)
            {
                removeBehaviour(this);
                decideCellToMoveTo(shipETA);
            }
        });
    }
    protected abstract void decideCellToMoveTo(String shipETA);

    protected void reserveSpaceInCell(String cellName)
    {
        DFAgentDescription[] cellAgentDescriptions = AgentUtils.searchDFbyName(this, cellName);
        if (cellAgentDescriptions.length != 1) throw new RuntimeException("Error in cell!");
        AID cell = cellAgentDescriptions[0].getName();

        AgentUtils.SendMessage(this, cell, ACLMessage.INFORM, "unloader-reserve-space", "Unloader informs incoming container");
    }

    private void requestETAofShipToDestination(String destination)
    {
        AgentUtils.SendMessage(this, portAgent, ACLMessage.REQUEST, "unloader-request-shipETA", destination);
    }

    private void requestContainerDestination(String containerName)
    {
        AID containerAgent = AgentUtils.searchDFbyName(this, containerName)[0].getName();
        AgentUtils.SendMessage(this, containerAgent, ACLMessage.REQUEST, "unloader-request-destination", "What is your destination?");
    }

    protected void operateCrane()
    {
        String  containerData = currentContainerName + ":" + currentDestination  + ":" + currentShipETA;
        AgentUtils.SendMessage(this, availableCranes.get(0), ACLMessage.REQUEST, "unloader-order-unload", containerData + "_" + shipAgent.getLocalName()  + "_" +  currentCellName);

        // get the next container from the ship
        doWait(750); // TODO: must find the "sweet spot with craneAgent waiting and this waiting for advancedUnloader to be able to work efficiently
        availableCells.clear();
        availableCranes.clear();
        addBehaviour(RequestContainerFromShip);
    }

    Behaviour sendCFPtoCranes = new TickerBehaviour(this, 100)
    {
        @Override
        public void onTick()
        {
            if (!availableCranes.isEmpty())
            {
                removeBehaviour(this);
                operateCrane();
                return;
            }

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

            for (AID craneAgent : craneAgents) {
                cfp.addReceiver(craneAgent);
            }

            cfp.setPerformative(ACLMessage.QUERY_IF);

            addBehaviour(new ProposeInitiator(myAgent, cfp)
            {
                protected void handleAcceptProposal(ACLMessage accept_proposal)
                {
                    availableCranes.add(accept_proposal.getSender());
                }

                protected void handleAllResponses(java.util.Vector responses)
                {
                    removeBehaviour(this);
                }
            });
        }
    };


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
