import classes.AgentUtils;
import classes.Utils;
import classes.Utils.Clock;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import static java.util.UUID.randomUUID;

public class MainAgent extends Agent
{
    private AgentContainer ac;
    private boolean clockRunning = true;
    private final int simulationSpeed = 1;
    Object[] PortArgs = {"3", "4", "5", "2", "1"};   // rows, columns, stackSize, noOfCranes, dockSize

    @Override
    protected void setup()
    {
        // the Agent registers itself to DF
        AgentUtils.registerToDF(this, getAID(), "MainAgent", "MainAgent");

        // start the clock
        Clock clock = new Clock(simulationSpeed);

        ac = getContainerController();

        try
        {
            // start GuiAgent
            AgentController Gui = ac.createNewAgent("GuiAgent", "GuiAgent", PortArgs);
            Gui.start();

            // wait for Gui to start // TODO: may need more time to start!
            Thread.sleep(3000);

            // initialize AgentUtils.Gui for global use
            AgentUtils.Gui gui = new AgentUtils.Gui(this);

            // start PortAgent
            AgentController Port = ac.createNewAgent("PortAgent", "PortAgent", PortArgs);
            Port.start();

            // create sample ship : ship058 with 18 containers to A, B and C
            createShip058();

            createEmptyShip(7, "A", "25");
            createEmptyShip(8, "B", "2000");
            createEmptyShip(9, "C", "2000");


        }
        catch (Exception e) {
            e.printStackTrace();
        }

        addBehaviour(RunClock);
        addBehaviour(ReceiveMessages);
    }

    Behaviour RunClock = new CyclicBehaviour(this)
    {
        @Override
        public void action()
        {
            if (clockRunning)
            {
                Clock.tick();
                AgentUtils.Gui.Send(myAgent, "clock-tick", String.valueOf(Clock.GetSimulationTime()));
                block(1000 / Clock.GetSimulationSpeed());
            }
        }
    };

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
                    case "play-pause":
                        clockRunning = !clockRunning;
                        AgentUtils.Gui.Send(myAgent, "console", "SIMULATION " + (clockRunning ? "RUNNING" : "PAUSED"));
                        break;
                }
            }

            block(10 / Utils.Clock.GetSimulationSpeed());
        }
    };

    private void createShip058() throws StaleProxyException {
        // create sample containers to add to ship
        Queue<String> containerAgents = new LinkedList<String>();

        for (int i = 0; i < 7; i++)
        {
            String containerName = String.valueOf(randomUUID());
            String destination = "A";
            AgentController Container = ac.createNewAgent(containerName, "ContainerAgent", new Object[]{containerName, destination});
            Container.start();
            containerAgents.add(containerName);
        }

        for (int i = 0; i < 6; i++)
        {
            String containerName = String.valueOf(randomUUID());
            String destination = "B";
            AgentController Container = ac.createNewAgent(containerName, "ContainerAgent", new Object[]{containerName, destination});
            Container.start();
            containerAgents.add(containerName);
        }

        for (int i = 0; i < 5; i++)
        {
            String containerName = String.valueOf(randomUUID());
            String destination = "C";
            AgentController Container = ac.createNewAgent(containerName, "ContainerAgent", new Object[]{containerName, destination});
            Container.start();
            containerAgents.add(containerName);
        }

        // TODO: implement a method to add a new ship with incremental ship number
        int shipNumber = 58;
        String shipName = "Ship";
        String arrivalTime = "5";
        String destination = "mainPort";
        shipName += shipNumber < 10 ? "00" : shipNumber < 100 ? "0" : "";
        shipName += shipNumber;
        AgentController Ship = ac.createNewAgent(shipName, "ShipAgent", new Object[]{shipName, arrivalTime, destination, containerAgents});
        Ship.start();
    }

    private void createEmptyShip(int shipNumber, String destination, String arrivalTime) throws StaleProxyException
    {
        Queue<String> containerAgents = new PriorityQueue<String>();

        // TODO: implement a method to add a new ship with incremental ship number
        String shipName = "Ship";
        shipName += shipNumber < 10 ? "00" : shipNumber < 100 ? "0" : "";
        shipName += shipNumber;
        AgentController Ship = ac.createNewAgent(shipName, "ShipAgent", new Object[]{shipName, arrivalTime, destination, containerAgents});
        Ship.start();
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
