import classes.AgentUtils;
import classes.CraneStatus;
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

public class CraneAgent extends Agent
{
    private CraneStatus status = CraneStatus.IDLE;
    private String craneName;
    private int currentTime = 0;


    @Override
    protected void setup()
    {
        Object[] CraneArgs = getArguments();
        craneName = (String)CraneArgs[0];

        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "CraneAgent", craneName);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF. Status is: " + status.toString());

        addBehaviour(ReceiveMessages);
        addBehaviour(respondCfp);
    }

    Behaviour ReceiveMessages = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);

            if (msg != null)
            {
                AgentUtils.Gui.Send(myAgent, "console", "Received message from: " +  msg.getSender().getLocalName() + " : " + msg.getContent());

                switch(msg.getOntology())
                {
                    case "unloader-order-unload":
                        status = CraneStatus.UNLOADING;
                        String[] infoParts = msg.getContent().split("_");
                        try {
                            MoveContainer(infoParts[0], infoParts[1], infoParts[2]);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case "loader-order-load":
                        status = CraneStatus.LOADING;
                        infoParts = msg.getContent().split("_");
                        try {
                            MoveContainer(infoParts[0], infoParts[1], infoParts[2]);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case "loader-order-move":
                        status = CraneStatus.MOVING;
                        infoParts = msg.getContent().split("_");
                        try {
                            MoveContainer(infoParts[0], infoParts[1], infoParts[2]);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void MoveContainer(String containerData, String sourceName, String destinationName) throws InterruptedException
    {
        String containerName = containerData.split(":")[0];
        AgentUtils.Gui.Send(this, "console", "Moving: " + containerName + " from " + sourceName + " to " + destinationName);
        AgentUtils.Gui.Send(this, "crane-moving-container", containerName + "_" + sourceName + "_" + destinationName + "_" + status.toString());

        DFAgentDescription[] destinationAgentDescriptions = AgentUtils.searchDFbyName(this, destinationName);
        if (destinationAgentDescriptions.length != 1) throw new RuntimeException("Error in cell!");
        AID destinationAgent = destinationAgentDescriptions[0].getName();

        // pause button functionality for cranes
        currentTime = Utils.Clock.GetSimulationTime();
        do { Thread.sleep(700 / Utils.Clock.GetSimulationSpeed()); } // simulate time passed to move container
        while (currentTime == Utils.Clock.GetSimulationTime());

        // inform destination that a container was given to it
        AgentUtils.SendMessage(this, destinationAgent, ACLMessage.INFORM, "crane-put-container", containerData);

        // update docked ship container count in gui
        if (status == CraneStatus.UNLOADING) { AgentUtils.Gui.Send(this, "crane-unloaded-ship", sourceName); }
        else if (status == CraneStatus.LOADING) { AgentUtils.Gui.Send(this, "crane-loaded-ship", destinationName); }
        else if (status == CraneStatus.MOVING) { AgentUtils.Gui.Send(this, "crane-moved-container", ""); }

        status = CraneStatus.IDLE;
    }

    Behaviour respondCfp = new ProposeResponder(this, MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF))
    {
        //@Override
        protected ACLMessage prepareResponse(ACLMessage proposal)
        {
            //AgentUtils.Gui.Send(myAgent, "console", "Received CPF. Status is " + status.toString()); // TODO: this is for debugging, delete this consoleLog later

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