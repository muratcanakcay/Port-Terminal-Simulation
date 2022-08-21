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
}