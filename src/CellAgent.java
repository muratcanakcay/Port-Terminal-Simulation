import classes.AgentUtils;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
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

        addBehaviour(respondCfp);
    }

    Behaviour respondCfp = new ProposeResponder(this, MessageTemplate.MatchAll())
    {
        //@Override
        protected ACLMessage prepareResponse(ACLMessage proposal)
        {
            int newContainerDepartureTime = Integer.parseInt(proposal.getContent());
            AgentUtils.Gui.Send(myAgent, "console", "Received CPF with departureTime : " + newContainerDepartureTime); // TODO: delete this consoleLog later


            if (containers.size() == stackSize)
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

                int score = 0; // TODO: calculate score based on existing containers and new container

                positiveReply.setContent(String.valueOf(score));
                return positiveReply;
            }
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
