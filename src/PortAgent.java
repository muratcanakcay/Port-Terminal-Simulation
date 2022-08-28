import classes.AgentUtils;
import classes.Port;
import classes.Utils;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class PortAgent extends Agent
{
    private Port port;

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "PortAgent", "PortAgent");

        // get arguments
        Object[] PortArgs = getArguments();
        int rows = Integer.parseInt((String)PortArgs[0]);
        int columns = Integer.parseInt((String)PortArgs[1]);
        int stackSize = Integer.parseInt((String)PortArgs[2]);
        int noOfCranes = Integer.parseInt((String)PortArgs[3]);

        // create ship object
        port = new Port(rows, columns, stackSize, noOfCranes);

        // start listening for messages
        addBehaviour(ReceiveMessages);






        AgentUtils.Gui.Send(this, "console", "Rows: " + port.getRows() + " Columns: " + port.getColumns() + " StackSize: " + port.getStackSize() + " noOfCranes: " + port.getNoOfCranes());

        AgentContainer ac = getContainerController();

        // Create the cell agents of the container storage
        try
        {
            for (int r = 0; r < rows; ++r)
            {
                for (int c = 0; c < columns; ++c)
                {
                    Object[] CellArgs = {String.valueOf(r), String.valueOf(c), String.valueOf(port.getStackSize())};
                    AgentController Cell = ac.createNewAgent("Cell" + r + ":" + c, "CellAgent", CellArgs);
                    Cell.start();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Create the craneAgents
        try
        {
            for (int i = 0; i < noOfCranes; ++i)
            {
                Object[] CraneArgs = {"Crane0" + i};
                AgentController Cell = ac.createNewAgent("Crane0" + i, "CraneAgent", CraneArgs);
                Cell.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Create the unloader
        try
        {
            String shipName = "Ship002";
            String unloaderName = "UnloaderFor" + shipName;
            Object[] UnloaderArgs = {unloaderName, shipName};
            AgentController Unloader = ac.createNewAgent(unloaderName, "UnloaderAgent", UnloaderArgs);
            Unloader.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    Behaviour ReceiveMessages = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            ACLMessage msg = receive();

            if (msg != null)
            {
                switch(msg.getOntology())
                {
                    case "ship-arrived":
                        AgentUtils.Gui.Send(myAgent, "console", "Received message: " + msg.getContent());

                        break;

                }
            }

            try {
                Thread.sleep(10 / Utils.Clock.GetSimulationSpeed()); // TODO: optimize sleep duration to ensure good messaging
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };
}
