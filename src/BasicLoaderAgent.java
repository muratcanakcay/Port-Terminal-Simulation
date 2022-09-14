import classes.AgentUtils;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Comparator;

public class BasicLoaderAgent extends LoaderAgent
{
    @Override
    protected void decideCellToLoadFrom()
    {
        // BasicLoader picks containers from the first eligible cell
        eligibleCells.sort(new SortByPosition());
        currentCellName = eligibleCells.get(0).split("@")[0];
        AID currentCellAID = AgentUtils.searchDFbyName(this, currentCellName)[0].getName();

        AgentUtils.Gui.Send(this, "console", "decided to load from " + currentCellName);
        AgentUtils.SendMessage(this, currentCellAID, ACLMessage.INFORM, "loader-request-container", "Requesting next container to load");

        // Find crane to move container
        addBehaviour(sendCFPtoCranes);
    }

    class SortByPosition implements Comparator<String>
    {
        public int compare(String cellA, String cellB)
        {
            String[] cellAinfoParts = (cellA.split("@"))[0].split(":");
            int aRow = Integer.parseInt(cellAinfoParts[1]);
            int aColumn = Integer.parseInt(cellAinfoParts[2]);
            int aPosition = (aRow * columns) + aColumn;

            String[] cellBinfoParts = (cellB.split("@"))[0].split(":");
            int bRow = Integer.parseInt(cellBinfoParts[1]);
            int bColumn = Integer.parseInt(cellBinfoParts[2]);
            int bPosition = (bRow * columns) + bColumn;

            return aPosition - bPosition;
        }
    }
}