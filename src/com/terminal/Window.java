package com.terminal;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Window
{
    public  JPanel mainPanel;
    private JTextField destination;
    private JTextField count;
    private JPanel port;
    private JButton drop;
    private JLabel Jlabel2;

    public Window(int rows, int columns, int stackSize)
    {

        Border blackline = BorderFactory.createLineBorder(Color.black);

        port.setLayout(new GridLayout(rows, columns));

        for (int i = 0; i < columns; i++) {

            List<JPanel> jPanelList = new ArrayList();
            for (int j = 0; j < rows; j++){
                JPanel jPanel = new JPanel();
                jPanel.setBorder(blackline);

                //add panel
                int colInRegion = 1;
                jPanel.setLayout(new GridLayout(stackSize, colInRegion));
                for (int k = 0; k < colInRegion; k++){
                    for (int l = 0; l < stackSize; l++){
                        JTextField jTextField = new JTextField();
                        jPanel.add(jTextField);
                    }
                }

                jPanelList.add(jPanel);
                port.add(jPanelList.get(j));
            }
        }

        List<JTextField> test2 = new ArrayList();
        Component[] children = port.getComponents();
        // iterate over all subPanels...
        int i = 0;
        for (Component spChild : children)
        {
            if (spChild instanceof JPanel)
            {
                JPanel jPanel = ((JPanel)spChild);

                Component[] childrenJtext = jPanel.getComponents();
                int j = 0;
                // now iterate over all JTextFields...
                for (Component spJTChild : childrenJtext)
                {
                    if (spJTChild instanceof JTextField)
                    {
                        JTextField jTextField = ((JTextField)spJTChild);
                        jTextField.setText("Test: "+ i + j);
                        j  = j+1;
                    }
                }

                System.out.println(i);

                i  = i+1;
            }
        }

//        JTextField test2[] = new JTextField[rows];
//        JTextField test3[] = new JTextField[rows];
//        JTextField test4[] = new JTextField[rows];
//        JTextField test5[] = new JTextField[rows];
//        for (int i = 0; i < rows; i++)
//        {
//            test1[i] = new JTextField();
//            test2[i] = new JTextField();
//            test3[i] = new JTextField();
//            test4[i] = new JTextField();
//            test5[i] = new JTextField();
//            mainPanel.add(test1[i]);
//            mainPanel.add(test2[i]);
//            mainPanel.add(test3[i]);
//            mainPanel.add(test4[i]);
//            mainPanel.add(test5[i]);
//        }


   }

//    public static void main(String[] args)
//    {
//
//        JFrame jFrame = new JFrame("Main Panel");
//        jFrame.setContentPane(new MainFrame().mainPanel);
//        jFrame.setTitle("Port Terminal Problem");
//        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        jFrame.pack();
//        jFrame.setVisible(true);
//    }
}
