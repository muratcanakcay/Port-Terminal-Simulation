import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class LoaderFactory
{
    AgentContainer ac;
    String loaderType;
    int columns;

    public LoaderFactory(AgentContainer _ac, String _loaderType, int _columns)
    {
        ac = _ac;
        loaderType = _loaderType;
        columns = _columns;
    }

    public AgentController createLoaderAgentFor(String shipName, String destination)
    {
        try
        {
            String loaderName = "LoaderFor" + shipName;
            Object[] LoaderArgs = {loaderName, shipName, destination, columns};
            AgentController Loader = ac.createNewAgent(loaderName, loaderType, LoaderArgs);
            Loader.start();
            return Loader;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
