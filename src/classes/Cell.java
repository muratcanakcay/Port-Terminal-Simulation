package classes;

public class Cell
{
    private final int row;                  // which row the cell is located at
    private final int column;               // which column the cell is located at
    private final int stackSize;            // stackSize of the cell -- MAY BE UNNECESSARY TO STORE HERE
    private final String name;              // name of the cell
    private final Container[] containers;   // containers stacked in the cell


    public Cell(int row, int column, int stackSize)
    {
        this.row = row;
        this.column = column;
        this.stackSize = stackSize;
        name = "CellAgent(" + row + "," + column + ")";
        containers = new Container[stackSize];
    }

    public String getName() { return name; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public Container[] getContainers() { return containers; }

    // TODO: probably will require methods related to containers
}