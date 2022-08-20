import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class PortAgent extends Agent
{
    int rows;
    int columns;
    int stackSize;

    @Override
    protected void setup()
    {
        Object[] PortArgs = getArguments();
        rows = Integer.parseInt((String)PortArgs[0]);
        columns = Integer.parseInt((String)PortArgs[1]);
        stackSize = Integer.parseInt((String)PortArgs[2]);

        System.out.println("[PORTAGENT] Rows: " + rows + " Columns: " + columns + " StackSize: " + stackSize);

        // the Agent registers itself to DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("Port");
        sd.setName("PortAgent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
