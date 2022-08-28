import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class guiWindow
{
    public  JPanel mainPanel;
    private JTextField destination;
    private JTextField count;
    private JPanel cellGrid;
    private JButton changeButton; // TODO: this is just for testing. remove later.
    private JLabel Jlabel2;
    private JScrollPane scrollPaneForConsole;
    private JPanel craneGrid;

    private final JTextPane console;
    public JTextPane getConsole()
    {
        return console;
    }




    public guiWindow(int rows, int columns, int stackSize, int noOfCranes)
    {
        Border blackline = BorderFactory.createLineBorder(Color.black);

        // initialize cellGrid
        cellGrid.setLayout(new GridLayout(rows, columns));
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < columns; c++)
            {
                JPanel cell = new JPanel();
                cell.setBorder(blackline);
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

        // craneGrid
        craneGrid.setLayout(new GridLayout(noOfCranes + 1, 5));

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
                    if (c==0) crane.setText("classes.Crane");
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

        // console area
        console = new JTextPane();
        console.setMargin(new Insets(20, 10, 55, 10));
        DefaultCaret caret = (DefaultCaret) console.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPaneForConsole.setViewportView(console);
        scrollPaneForConsole.getPreferredSize();



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
