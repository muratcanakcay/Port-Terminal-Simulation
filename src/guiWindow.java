import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class guiWindow
{
    public  JPanel mainPanel;
    private JPanel cellGrid;
    public JPanel getCellGrid() { return cellGrid; };
    private JButton changeButton; // TODO: this is just for testing. remove later.
    private JScrollPane scrollPaneForConsole;
    private JScrollPane scrollPaneForWaitingShips;
    private JScrollPane scrollPaneForIncomingShips;
    private JPanel craneGrid;
    public JPanel getCraneGrid()
    {
        return craneGrid;
    }
    private JPanel simulationTimePanel;
    public JPanel getSimulationTimePanel() { return simulationTimePanel; }
    private JPanel dockGrid;
    public JPanel getDockGrid()
    {
        return dockGrid;
    }
    private final JTextPane console;
    public JTextPane getConsole()
    {
        return console;
    }
    private final JTextPane waitingShips;
    public JTextPane getWaitingShips()
    {
        return waitingShips;
    }
    private final JTextPane incomingShips;
    public JTextPane getIncomingShips()
    {
        return incomingShips;
    }
    private Button pauseButton = new Button("Pause");
    public Button getPauseButton() { return pauseButton; }
    private final Border blackLine = BorderFactory.createLineBorder(Color.black);

    public guiWindow(int rows, int columns, int stackSize, int noOfCranes, int dockSize)
    {
        simulationTimePanel.setLayout(new GridLayout(1, 10));

        // initialize the GUI components
        initializeClockPanel();
        initializeMoveCountPanel("Unload Moves: ");
        initializeMoveCountPanel("Load Moves: ");
        initializeMoveCountPanel("Internal Moves: ");
        initializeMoveCountPanel("Total Moves: ");
        initializeCellGrid(rows, columns, stackSize);
        initializeCraneGrid(noOfCranes);
        initializeDockGrid(dockSize);
        console = initializeScrollPane(scrollPaneForConsole);
        waitingShips = initializeScrollPane(scrollPaneForWaitingShips);
        incomingShips = initializeScrollPane(scrollPaneForIncomingShips);
    }

    private JTextPane initializeScrollPane(JScrollPane scrollPane)
    {
        final JTextPane textPane;
        textPane = new JTextPane();
        textPane.setMargin(new Insets(20, 10, 55, 10));
        DefaultCaret consoleCaret = (DefaultCaret) textPane.getCaret();
        consoleCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPane.setViewportView(textPane);
        scrollPane.getPreferredSize();
        return textPane;
    }

    private void initializeDockGrid(int dockSize)
    {
        dockGrid.setLayout(new GridLayout(dockSize + 1, 5)); // first is row for headings
        for (int r = 0; r < dockSize + 1; r++)
        {
            for (int c = 0; c < 5; c++)
            {
                JTextField ship = new JTextField();
                ship.setEditable(false);
                if (r == 0)
                {
                    Font boldShipTextFont = new Font(ship.getFont().getName(), Font.BOLD, ship.getFont().getSize());
                    ship.setFont(boldShipTextFont);
                    if (c==0) ship.setText("Ship Name");
                    if (c==1) ship.setText("Status");
                    if (c==2) ship.setText("Containers");
                    if (c==3) ship.setText("Arrival");
                    if (c==4) ship.setText("Destination");
                }
                else
                {
                    ship.setText("");
                    ship.setBackground(Color.WHITE);
                }

                dockGrid.add(ship);
            }
        }
    }

    private void initializeCraneGrid(int noOfCranes) {
        craneGrid.setLayout(new GridLayout(noOfCranes + 1, 5)); // first is row for headings
        for (int r = 0; r < noOfCranes + 1; r++)
        {
            for (int c = 0; c < 5; c++)
            {
                JTextField crane = new JTextField();
                crane.setEditable(false);
                if (r != 0) crane.setBackground(Color.WHITE);
                if (r == 0)
                {
                    Font boldCraneTextFont = new Font(crane.getFont().getName(), Font.BOLD, crane.getFont().getSize());
                    crane.setFont(boldCraneTextFont);
                    if (c==0) crane.setText("Crane No.");
                    if (c==1) crane.setText("Status");
                    if (c==2) crane.setText("Container");
                    if (c==3) crane.setText("From");
                    if (c==4) crane.setText("To");
                }
                else
                {
                    if (c==0) crane.setText("Crane0" + r);
                    if (c==1) crane.setText("IDLE");
                }

                craneGrid.add(crane);
            }
        }
    }

    private void initializeCellGrid(int rows, int columns, int stackSize) {
        cellGrid.setLayout(new GridLayout(rows, columns));
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < columns; c++)
            {
                JPanel cell = new JPanel();
                cell.setBorder(blackLine);
                cell.setLayout(new GridLayout(stackSize, 1));

                for (int s = 0; s < stackSize; s++)
                {
                    JTextField stack = new JTextField();
                    stack.setEditable(false);
                    stack.setBackground(Color.WHITE);
                    //stack.setText("Test: " + r + c);
                    stack.setText("Empty");
                    cell.add(stack);
                }

                cellGrid.add(cell);
            }
        }
    }

    private void initializeClockPanel()
    {
        JTextField clock = new JTextField();
        clock.setEditable(false);
        Font boldClockFont = new Font(clock.getFont().getName(), Font.BOLD, 16);
        clock.setFont(boldClockFont);
        clock.setBackground(Color.WHITE);
        clock.setText("0");
        clock.setHorizontalAlignment(JTextField.RIGHT);

        JPanel clockPanel = new JPanel();
        clockPanel.setLayout(new BorderLayout());
        clockPanel.setBorder(blackLine);

        clockPanel.add(clock);

        JPanel pauseButtonPanel = new JPanel();
        pauseButtonPanel.setBorder(blackLine);
        pauseButtonPanel.add(pauseButton);

        simulationTimePanel.add(clockPanel);
        simulationTimePanel.add(pauseButtonPanel);
    }

    private void initializeMoveCountPanel(String label)
    {
        JTextField moveCount = new JTextField();
        moveCount.setEditable(false);
        Font boldClockFont = new Font(moveCount.getFont().getName(), Font.BOLD, 16);
        moveCount.setFont(boldClockFont);
        moveCount.setBackground(Color.WHITE);
        moveCount.setText("0");
        moveCount.setHorizontalAlignment(JTextField.RIGHT);

        JPanel moveCountPanel = new JPanel();
        moveCountPanel.setLayout(new BorderLayout());
        moveCountPanel.setBorder(blackLine);

        moveCountPanel.add(moveCount);

        JPanel moveCountLabelPanel = new JPanel();
        moveCountLabelPanel.setBorder(blackLine);
        moveCountLabelPanel.setBackground(Color.WHITE);
        moveCountLabelPanel.add(new Label(label));

        simulationTimePanel.add(moveCountLabelPanel);
        simulationTimePanel.add(moveCountPanel);
    }
}

