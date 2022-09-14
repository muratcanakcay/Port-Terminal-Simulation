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
    private AID container = null;
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
                            UnloadContainer(infoParts[0], infoParts[1], infoParts[2]);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case "loader-order-load":
                        status = CraneStatus.LOADING;
                        infoParts = msg.getContent().split("_");
                        try {
                            LoadContainer(infoParts[0], infoParts[1], infoParts[2]);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void UnloadContainer(String containerData, String shipName, String cellName) throws InterruptedException
    {
        String containerName = containerData.split(":")[0];
        AgentUtils.Gui.Send(this, "console", "Moving: " + containerName + " from " + shipName + " to " + cellName);
        AgentUtils.Gui.Send(this, "crane-moving-container", containerName + "_" + shipName + "_" + cellName + "_" + status.toString());

        DFAgentDescription[] cellAgentDescriptions = AgentUtils.searchDFbyName(this, cellName);
        if (cellAgentDescriptions.length != 1) throw new RuntimeException("Error in cell!");
        AID cell = cellAgentDescriptions[0].getName();

        // pause button functionality for cranes
        currentTime = Utils.Clock.GetSimulationTime();
        do { Thread.sleep(1000 / Utils.Clock.GetSimulationSpeed()); } // simulate time passed to move container
        while (currentTime == Utils.Clock.GetSimulationTime());

        AgentUtils.SendMessage(this, cell, ACLMessage.INFORM, "crane-put-container", containerData);
        AgentUtils.Gui.Send(this, "crane-unloaded-ship", shipName); // update docked ship container count in gui
        status = CraneStatus.IDLE;
    }

    private void LoadContainer(String containerData, String cellName, String shipName) throws InterruptedException
    {
        String containerName = containerData.split(":")[0];
        AgentUtils.Gui.Send(this, "console", "Moving: " + containerName + " from " + cellName + " to " + shipName);
        AgentUtils.Gui.Send(this, "crane-moving-container", containerName + "_" + cellName + "_" + shipName + "_" + status.toString());

        DFAgentDescription[] shipAgentDescriptions = AgentUtils.searchDFbyName(this, shipName);
        if (shipAgentDescriptions.length != 1) throw new RuntimeException("Error in ship!");
        AID shipAgent = shipAgentDescriptions[0].getName();

        // pause button functionality for cranes
        currentTime = Utils.Clock.GetSimulationTime();
        do { Thread.sleep(1000 / Utils.Clock.GetSimulationSpeed()); } // simulate time passed to move container
        while (currentTime == Utils.Clock.GetSimulationTime());

        AgentUtils.SendMessage(this, shipAgent, ACLMessage.INFORM, "crane-put-container", containerData);
        AgentUtils.Gui.Send(this, "crane-loaded-ship", shipName); // update docked ship container count in gui
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