import classes.AgentUtils;
import classes.CraneStatus;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;

public class CraneAgent extends Agent
{
    private CraneStatus status = CraneStatus.IDLE;
    String craneName;
    AID container = null;



    @Override
    protected void setup()
    {
        Object[] CraneArgs = getArguments();
        craneName = (String)CraneArgs[0];

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "CraneAgent", craneName);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF. Status is: " + status.toString());

        addBehaviour(respondCfp);
    }

    Behaviour respondCfp = new ProposeResponder(this, MessageTemplate.MatchAll())
    {
        //@Override
        protected ACLMessage prepareResponse(ACLMessage proposal)
        {
            AgentUtils.Gui.Send(myAgent, "console", "Received CPF. Status is " + status.toString()); // TODO: this is for debugging, delete this consoleLog later

            if (status == CraneStatus.IDLE) {
                ACLMessage positiveReply = proposal.createReply();
                positiveReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                positiveReply.setContent("This crane is " + status.toString());
                return positiveReply;
            }
            else
            {
                ACLMessage negativeReply = proposal.createReply();
                negativeReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                negativeReply.setContent("This crane is " + status.toString());
                return negativeReply;
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