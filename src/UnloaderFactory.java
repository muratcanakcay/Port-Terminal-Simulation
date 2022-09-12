import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class UnloaderFactory
{
    AgentContainer ac;
    String unloaderType;
    int columns;

    public UnloaderFactory(AgentContainer _ac, String _unloaderType, int _columns)
    {
        ac = _ac;
        unloaderType = _unloaderType;
        columns = _columns;
    }

    public AgentController createUnloaderAgentFor(String shipName)
    {
        try
        {
            String unloaderName = "UnloaderFor" + shipName;
            Object[] UnloaderArgs = {unloaderName, shipName, columns};
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
