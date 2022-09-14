import classes.AgentUtils;
import jade.lang.acl.ACLMessage;

import java.util.Comparator;
import java.util.Objects;

public class AdvancedLoaderAgent extends LoaderAgent
{
    @Override
    protected void decideCellToMoveTo()
    {
        // AdvancedUnloader tries to place the container in a cell where all of the containers below
        // have a larger shipETA value

        availableCells.sort(new SortByFeasability());
        currentDestinationCellName = availableCells.get(0).split("@")[0];
        reserveSpaceInCell(currentDestinationCellName);
        String[] containersInCurrentCell = eligibleCells.get(0).split("_");
        String containerData = containersInCurrentCell[containersInCurrentCell.length - 1];
        AgentUtils.SendMessage(this, availableCranes.get(0), ACLMessage.REQUEST, "loader-order-move", containerData + "_" + currentCellName + "_" + currentDestinationCellName);

        // get the next container from the ship
        doWait(750);
        eligibleCells.clear();
        availableCranes.clear();
        sendCFPtoCells(destination);
    }

    class SortByFeasability implements Comparator<String>
    {
        public int compare(String cellA, String cellB)
        {
            int fA = calculateFeasabilityScore(cellA);
            int fB = calculateFeasabilityScore(cellB);
            if (fA == fB) return calculatePosition(cellA) - calculatePosition(cellB);
            return fA-fB;
        }

        private int calculateFeasabilityScore(String cellInfo)
        {
            String[] containersInCell = cellInfo.split("_");

            if (containersInCell.length == 1) return 0; // empty cell (first position is cell name

            int score = 0;
            for (int i = 1; i < containersInCell.length; ++i)
            {
                String destination = containersInCell[i].split(":")[1];
                int shipETA = Integer.parseInt(containersInCell[i].split(":")[2]);

                if (Objects.equals(destination, currentDestination)) score += -5;
                else if (shipETA > currentShipETA) score += 1;
                else if (shipETA < currentShipETA) score += 5;
            }

            System.out.println("Feasability score for " + currentCellName + " is " + score);

            return score;
        }

        private int calculatePosition(String cellInfo)
        {
            String[] cellInfoParts = (cellInfo.split("@"))[0].split(":");
            int Row = Integer.parseInt(cellInfoParts[1]);
            int Column = Integer.parseInt(cellInfoParts[2]);
            return (Row * columns) + Column;
        }
    }

    @Override
    protected void decideCellToLoadFrom()
    {
        // BasicLoader picks containers from the first eligible cell
        eligibleCells.sort(new SortByPosition());
        currentCellName = eligibleCells.get(0).split("@")[0];
        currentCellAID = AgentUtils.searchDFbyName(this, currentCellName)[0].getName();

        AgentUtils.Gui.Send(this, "console", "decided to load from " + currentCellName);

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