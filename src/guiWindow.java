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

    private final JTextPane console;
    public JTextPane getConsole()
    {
        return console;
    }




    public guiWindow(int rows, int columns, int stackSize)
    {
        Border blackline = BorderFactory.createLineBorder(Color.black);

        cellGrid.setLayout(new GridLayout(rows, columns));
        console = new JTextPane();
        console.setMargin(new Insets(20, 10, 55, 10));
        DefaultCaret caret = (DefaultCaret) console.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        scrollPaneForConsole.setViewportView(console);
        scrollPaneForConsole.getPreferredSize();


        for (int r = 0; r < columns; r++)
        {
            for (int c = 0; c < rows; c++)
            {
                JPanel cell = new JPanel();
                cell.setBorder(blackline);

                cell.setLayout(new GridLayout(stackSize, 1));
                for (int s = 0; s < stackSize; s++)
                    cell.add(new JTextField());

                cellGrid.add(cell);
            }
        }

        Component[] cells = cellGrid.getComponents();
        // iterate over cells...
        int i = 0;
        for (Component cellComponent : cells)
        {
            JPanel cell = ((JPanel)cellComponent);
            Component[] stacks = cell.getComponents();

            // iterate over stacks...
            int j = 0;
            for (Component stackComponent : stacks)
            {
                JTextField stack = ((JTextField)stackComponent);
                stack.setText("Test: "+ i + j);
                ++j;
            }

            ++i;
        }

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
            }
        });
    }
}
