package classes;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class AgentUtils
{
    public static class Gui
    {
        private static jade.core.AID GuiAgent;

        public Gui(jade.core.Agent agent)
        {
            // get the GuiAgent AID from DF
            GuiAgent = searchDF(agent, "GuiAgent", "GuiAgent")[0].getName();
        }

        // send a String message to GuiAgent with defined ontology
        public static void Send(jade.core.Agent agent, String ontology, String content)
        {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(GuiAgent);
            msg.setOntology(ontology);
            msg.setContent(content);
            agent.send(msg);
        }
    }

    public static void registerToDF(jade.core.Agent agent, jade.core.AID aid, String agentType, String agentName)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(aid);

        ServiceDescription sd = new ServiceDescription();
        // TODO: check if can be searched just by name or type. So maybe if type or name == null don't set it?
        sd.setType(agentType);
        sd.setName(agentName);
        dfd.addServices(sd);
        try {
            DFService.register(agent, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public static DFAgentDescription[] searchDF(jade.core.Agent agent, String type, String name)
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(type);
        sd.setName(name);
        template.addServices(sd);

        try {
            return DFService.search(agent, template);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        return new DFAgentDescription[0];
    }
}