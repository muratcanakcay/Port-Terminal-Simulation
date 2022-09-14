import java.util.Comparator;
import java.util.Objects;

public class AdvancedUnloaderAgent extends UnloaderAgent
{
    @Override
    protected void makeDecision(String shipETA)
    {
        // AdvancedUnloader tries to place the container in a cell where all of the containers below
        // have a larger shipETA value

        availableCells.sort(new SortByFeasability());
        currentCellName = availableCells.get(0).split("@")[0];

        reserveSpaceInCell(currentCellName);

        // Find crane to move container
        addBehaviour(sendCFPtoCranes);
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
}
