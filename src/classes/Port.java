package classes;

// TODO: Port class will probably also hold all incoming ships, list of cranes ( Crane[] ) etc. Crane class should be implemented

public class Port
{
    private final int rows;
    private final int columns;
    private final int stackSize;

    public Port(int rows, int columns, int stackSize)
    {
        this.rows = rows;
        this.columns = columns;
        this.stackSize = stackSize;
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getStackSize() { return stackSize; }
}
