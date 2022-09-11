import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class UnloaderFactory
{
    AgentContainer ac;
    String unloaderType;

    public UnloaderFactory(AgentContainer _ac, String _unloaderType)
    {
        ac = _ac;
        unloaderType = _unloaderType;
    }

    public AgentController createUnloaderAgentFor(String shipName)
    {
        try
        {
            String unloaderName = "UnloaderFor" + shipName;
            Object[] UnloaderArgs = {unloaderName, shipName};
            AgentController Unloader = ac.createNewAgent(unloaderName, unloaderType, UnloaderArgs);
            Unloader.start();
            return Unloader;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
