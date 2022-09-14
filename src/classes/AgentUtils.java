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
            GuiAgent = searchDFbyName(agent, "GuiAgent")[0].getName();
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

        sd.setType(agentType);
        sd.setName(agentName);
        dfd.addServices(sd);

        try {
            DFService.register(agent, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public static DFAgentDescription[] searchDFbyName(jade.core.Agent agent, String name)
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

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

    public static DFAgentDescription[] searchDFbyType(jade.core.Agent agent, String type)
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();

        sd.setType(type);
        template.addServices(sd);

        try {
            return DFService.search(agent, template);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        return new DFAgentDescription[0];
    }

    public static void SendMessage(jade.core.Agent agent, jade.core.AID receiver, int perf, String ontology, String content)
    {
        ACLMessage msg = new ACLMessage(perf);
        msg.addReceiver(receiver);
        msg.setOntology(ontology);
        msg.setContent(content);
        agent.send(msg);
    }
}