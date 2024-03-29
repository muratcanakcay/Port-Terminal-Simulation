import classes.AgentUtils;
import classes.Utils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
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

public abstract class LoaderAgent extends Agent
{
    private String name;
    protected int columns;
    protected String destination;
    private String currentContainerName = "";
    protected String currentCellName;
    protected AID currentCellAID;
    protected String currentDestinationCellName;
    protected String currentDestination;
    protected int currentShipETA;
    protected AID shipAgent;
    private AID portAgent;
    private final List<AID> cellAgents = new ArrayList<AID>();
    private final List<AID> craneAgents = new ArrayList<AID>();
    protected final List<String> eligibleCells = new ArrayList<>();
    protected final List<String> availableCells = new ArrayList<>();
    protected final List<AID> availableCranes = new ArrayList<>();


    @Override
    protected void setup() {
        Object[] LoaderArgs = getArguments();
        name = (String)LoaderArgs[0];
        String shipName = (String)LoaderArgs[1];
        destination = (String)LoaderArgs[2];
        columns = (int) LoaderArgs[3];

        DFAgentDescription[] ships = AgentUtils.searchDFbyName(this, shipName);

        // agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "LoaderAgent", name);
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
        sendCFPtoCells(destination);
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
                    case "container-destination":
                        currentDestination = msg.getContent();
                        requestETAofShipToDestination(currentDestination);
                        break;
                    case "port-shipETA":
                        currentShipETA = Integer.parseInt(msg.getContent());
                        if (currentShipETA < 0) { AgentUtils.Gui.Send(myAgent, "console-error", "There's no ship that will take " + currentContainerName + " to destination " + currentDestination); }
                        //sendCFPtoCells(msg.getContent());
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void concludeLoadingProcedure()
    {
        doWait(6000);
        AgentUtils.SendMessage(this, portAgent, ACLMessage.INFORM, "loader-loading-finished", "Loading of :" + shipAgent.getLocalName() + ": is finished");
        doDelete();
    }

    protected void sendCFPtoCells(String shipDestination)
    {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

        for (AID cellAgent : cellAgents)
        {
            cfp.addReceiver(cellAgent);
        }

        cfp.setContent(shipDestination);
        cfp.setPerformative(ACLMessage.QUERY_IF);

        addBehaviour(new ProposeInitiator(this, cfp)
        {
            protected void handleAcceptProposal(ACLMessage accept_proposal)
            {
                String cellContents = accept_proposal.getContent();
                String repliedCell = accept_proposal.getSender().getName() + "_" + cellContents;

                if (Objects.equals(shipDestination, "")) // CFP for available cells to move a container to
                {
                    availableCells.add(repliedCell);
                }
                else // CFP for eligible cells to pick a container from
                {
                    eligibleCells.add(repliedCell);
                }
            };

            protected void handleAllResponses(java.util.Vector responses)
            {
                removeBehaviour(this);

                if (Objects.equals(shipDestination, ""))  // CFP for available cells to move a container to
                {
                    decideCellToMoveTo();
                }
                else
                {
                    // if there are no cells containing containers headed for destination, inform port and terminate
                    if (eligibleCells.isEmpty())
                    { concludeLoadingProcedure(); }
                    else
                    { decideCellToLoadFrom(); }
                }
            }
        });
    }

    protected abstract void decideCellToMoveTo();

    protected abstract void decideCellToLoadFrom();

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

    protected void operateCrane()
    {
        String[] containersInCurrentCell = eligibleCells.get(0).split("_");
        String containerData = containersInCurrentCell[containersInCurrentCell.length - 1];
        String containerDestination = containerData.split(":")[1];

        AgentUtils.SendMessage(this, currentCellAID, ACLMessage.INFORM, "loader-request-container", "Requesting next container to load");

        // if container destination is shipDestination load to ship, otherwise move in storage
        if (Objects.equals(containerDestination, destination))
        {
            AgentUtils.SendMessage(this, availableCranes.get(0), ACLMessage.REQUEST, "loader-order-load", containerData + "_" + currentCellName + "_" + shipAgent.getLocalName());

            // get the next container from the ship
            doWait(750);
            eligibleCells.clear();
            availableCranes.clear();
            sendCFPtoCells(destination);
        }
        else
        {
            AgentUtils.Gui.Send(this, "console", "sendCFP for availability");
            sendCFPtoCells("");
        }
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
