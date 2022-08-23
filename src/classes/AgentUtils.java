package classes;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class AgentUtils
{
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