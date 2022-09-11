import classes.AgentUtils;
import classes.Utils;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class ContainerAgent extends Agent
{
    private String containerName;
    private String destination;

    @Override
    protected void setup()
    {
        Object[] ContainerArgs = getArguments();
        containerName  = (String)ContainerArgs[0];
        destination = (String)ContainerArgs[1];
//        int departureTime = Integer.parseInt((String)ContainerArgs[2]);


        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "ContainerAgent", containerName);
        AgentUtils.Gui.Send(this, "console", "Agent is registered to DF.");

        addBehaviour(ReceiveMessages);
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
                    case "unloader-request-destination":
                        sendDestinationToUnloader(msg);
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void sendDestinationToUnloader(ACLMessage msg)
    {
        ACLMessage response = msg.createReply();
        response.setPerformative(ACLMessage.INFORM);
        response.setOntology("container-destination");
        response.setContent(destination);
        send(response);
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
