package classes;

// TODO: Port class will probably also hold all incoming ships, list of cranes ( classes.Crane[] ) etc. classes.Crane class should be implemented

public class Port
{
    private final int rows;
    private final int columns;
    private final int stackSize;
    private final int noOfCranes;

    public Port(int rows_, int columns_, int stackSize_, int noOfCranes_)
    {
        rows = rows_;
        columns = columns_;
        stackSize = stackSize_;
        noOfCranes = noOfCranes_;
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getStackSize() { return stackSize; }
    public int getNoOfCranes() { return noOfCranes; }
}
