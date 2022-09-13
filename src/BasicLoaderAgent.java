import java.util.Comparator;

public class BasicLoaderAgent extends LoaderAgent
{
    @Override
    protected void makeDecision()
    {
        // BasicLoader places container to the first available place
        eligibleCells.sort(new SortByPosition());
        currentCellName = eligibleCells.get(0).split("@")[0];

        reserveSpaceInCell(currentCellName);

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