import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class guiWindow
{
    public  JPanel mainPanel;
    private JTextField count;
    private JPanel cellGrid;
    private JButton changeButton; // TODO: this is just for testing. remove later.
    private JLabel Jlabel2;
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

    public guiWindow(int rows, int columns, int stackSize, int noOfCranes, int dockSize)
    {
        Border blackLine = BorderFactory.createLineBorder(Color.black);

        // initialize clockPanel
        simulationTimePanel.setLayout(new GridLayout(1, 2));


        JTextField clock = new JTextField();
        clock.setEditable(false);
        Font boldClockFont = new Font(clock.getFont().getName(), Font.BOLD, 16);
        clock.setFont(boldClockFont);
        clock.setBackground(Color.WHITE);
//        Insets clockMargin = new Insets(5, 5, 5, 5);
//        clock.setMargin(clockMargin);
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


        // initialize cellGrid
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

        // initialize craneGrid
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

        // initialize dockGrid
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

        // TODO: create a grid for incoming/waiting ships

        // initialize console area
        console = new JTextPane();
        console.setMargin(new Insets(20, 10, 55, 10));
        DefaultCaret consoleCaret = (DefaultCaret) console.getCaret();
        consoleCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPaneForConsole.setViewportView(console);
        scrollPaneForConsole.getPreferredSize();

        // initialize waiting ships area
        waitingShips = new JTextPane();
        waitingShips.setMargin(new Insets(20, 10, 55, 10));
        DefaultCaret waitingShipsCaret = (DefaultCaret) waitingShips.getCaret();
        waitingShipsCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPaneForWaitingShips.setViewportView(waitingShips);
        scrollPaneForWaitingShips.getPreferredSize();

        // initialize incoming ships area
        incomingShips = new JTextPane();
        incomingShips.setMargin(new Insets(20, 10, 55, 10));
        DefaultCaret incomingShipsCaret = (DefaultCaret) incomingShips.getCaret();
        incomingShipsCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPaneForIncomingShips.setViewportView(incomingShips);
        scrollPaneForIncomingShips.getPreferredSize();



//        Component[] cells = cellGrid.getComponents();
//        // iterate over cells...
//        int i = 0;
//        for (Component cellComponent : cells)
//        {
//            JPanel cell = ((JPanel)cellComponent);
//            Component[] stacks = cell.getComponents();
//
//            // iterate over stacks...
//            int j = 0;
//            for (Component stackComponent : stacks)
//            {
//                JTextField stack = ((JTextField)stackComponent);
//                stack.setText("Test: "+ i + j);
//                ++j;
//            }
//            ++i;
//        }

        // TODO: Change to behaviour listening from cellAgent and changing Gui accordingly
        changeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int r = 1;
                int c = 2;

                Component[] panels = cellGrid.getComponents();
                JPanel panel = ((JPanel)panels[r*columns + c]);

                Component[] stacks = panel.getComponents();
                JTextField stack = ((JTextField)stacks[2]);

                stack.setText("Changed");
                //stack.setBackground(Color.RED);
            }
        });
    }
}
