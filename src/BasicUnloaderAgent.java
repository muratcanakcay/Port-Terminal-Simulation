import classes.AgentUtils;

public class BasicUnloaderAgent extends UnloaderAgent
{
    @Override
    protected void makeDecision()
    {
        //AgentUtils.Gui.Send(this, "console-error", "Placed container " + currentContainerName);
        AgentUtils.Gui.Send(this, "unloader-unloaded-ship", shipAgent.getLocalName());
        doWait(3000); // simulating new container unloading
        addBehaviour(RequestContainerFromShip); // TODO: this should actually be called when the current container is finally handed to the crane
    }
}