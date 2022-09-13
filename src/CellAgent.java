import classes.AgentUtils;
import classes.Utils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;

import java.util.Stack;

public class CellAgent extends Agent
{
    private int row;
    private int column;
    private int stackSize;
    private int reserved = 0;
    private String cellName;
    private final Stack<AID> containers = new Stack<AID>();

    @Override
    protected void setup()
    {
        Object[] CellArgs = getArguments();
        row = Integer.parseInt((String)CellArgs[0]);
        column = Integer.parseInt((String)CellArgs[1]);
        stackSize = Integer.parseInt((String)CellArgs[2]);
        cellName = "Cell:" + row + ":" + column;

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "CellAgent", cellName);

        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        addBehaviour(ReceiveMessages);
        addBehaviour(respondCfp);
    }

    Behaviour ReceiveMessages = new CyclicBehaviour(this) {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);

            if (msg != null)
            {
                AgentUtils.Gui.Send(myAgent, "console", "Received message from: " + msg.getSender().getLocalName() + " : " + msg.getContent());

                switch (msg.getOntology()) {
                    case "crane-put-container":
                        String containerData = msg.getContent(); // data format: containerName:destination:shipETA
                        receiveContainer(containerData);
                        break;
                    case "unloader-reserve-space":
                        ++reserved;
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    Behaviour respondCfp = new ProposeResponder(this, MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF))
    {
        //@Override
        protected ACLMessage prepareResponse(ACLMessage proposal)
        {
            int newContainerDepartureTime = Integer.parseInt(proposal.getContent());
            //AgentUtils.Gui.Send(myAgent, "console", "Received CPF with departureTime : " + newContainerDepartureTime); // TODO: this is for debugging, delete this consoleLog later


            if (containers.size() + reserved == stackSize)
            {
                ACLMessage negativeReply = proposal.createReply();
                negativeReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                negativeReply.setContent("This cell is full");
                return negativeReply;
            }
            else
            {
                ACLMessage positiveReply = proposal.createReply();
                positiveReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                int score = 0; // TODO: send the shipETAs instead of score and let the loader/unloader calculate score

                positiveReply.setContent(String.valueOf(score));
                return positiveReply;
            }
        }
    };
    private void receiveContainer(String containerData)
    {
        String containerName = containerData.split(":")[0];

        DFAgentDescription[] containerAgentDescriptions = AgentUtils.searchDFbyName(this, containerName);
        if (containerAgentDescriptions.length != 1) throw new RuntimeException("Error in container!");
        AID receivedContainer = containerAgentDescriptions[0].getName();

        containers.push(receivedContainer);
        --reserved;

        // TODO: send also the destination and pickup time to GUI
        AgentUtils.Gui.Send(this, "cell-received-container", (containers.size() - 1) + ":" + containerData);
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


