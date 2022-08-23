import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window
{
    public  JPanel mainPanel;
    private JTextField destination;
    private JTextField count;
    private JPanel port;
    private JButton changeButton; // TODO: this is just for testing. remove later.
    private JLabel Jlabel2;

    public JTextArea getConsoleLog() {
        return consoleLog;
    }

    private JTextArea consoleLog;



    public Window(int rows, int columns, int stackSize)
    {
        Border blackline = BorderFactory.createLineBorder(Color.black);

        port.setLayout(new GridLayout(rows, columns));

        for (int r = 0; r < columns; r++)
        {
            for (int c = 0; c < rows; c++)
            {
                JPanel cell = new JPanel();
                cell.setBorder(blackline);

                cell.setLayout(new GridLayout(stackSize, 1));
                for (int s = 0; s < stackSize; s++)
                    cell.add(new JTextField());

                port.add(cell);
            }
        }

        Component[] cells = port.getComponents();
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

                Component[] panels = port.getComponents();
                JPanel panel = ((JPanel)panels[r*columns + c]);

                Component[] stacks = panel.getComponents();
                JTextField stack = ((JTextField)stacks[2]);

                stack.setText("Changed");
            }
        });
    }
}
