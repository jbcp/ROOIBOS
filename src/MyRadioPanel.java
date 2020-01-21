
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class MyRadioPanel extends JPanel implements ActionListener {

    final String[] groupName = {"A", "B", "C", "D", "E"};//, "F", "G", "H", "I", "J"};
    private String[] selectedValueArr;
    private int width0 = 55;
    private int width1;
    private ButtonGroup[] buttonGroups;
    private JTable table;

    public MyRadioPanel() {
        makeTable(1, 20, 1);
      //  getRadioPanel();
        // setRadioPanel(1,20,1);
    }

    public MyRadioPanel(int start, int end, int groupNum) {

        makeTable(start, end, groupNum);   // setRadioPanel(start, end, groupNum);
        // setRadioPanel(1,20,1);
    }
    // String[] selectedValuesArr;
//    int selectedRow = -1;

//    public boolean selectedValuesArr(String s) {
//        try {
//            if (selectedRow >= 0) {
//                this.selectedValuesArr[selectedRow] = s;
//                selectedRow = -1;
//                return true;
//            } else {
//                return false;
//            }
//        } catch (Exception e) {
//            return false;
//        }
//    }
    public void setColumnWidth(int col, int width) {

    }
public String[] getSelectedValues(){
// System.out.println(buttonGroups[0]+"\t"+buttonGroups[0].getSelection().isSelected());
     selectedValueArr = new String[buttonGroups.length];
                for (int k = 0; k < buttonGroups.length; k++) {

                    String s = getSelectedButtonText(buttonGroups[k]);
                    //   System.out.println(k + "-->" + s);
                    selectedValueArr[k] = s;
                }
                
//                  System.out.println("----"+selectedValueArr[0]);
//        if (selectedValueArr == null) {
//            selectedValueArr = new String[]{"A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A"};
//        }
// 
    //    System.out.println("----"+selectedValueArr[0]);
        return selectedValueArr;
  
}
    public DefaultTableModel makeModel(int start, int end, int groupNum) {
        width1 = 35 * groupNum;

        int pNum = end - start + 1;

        buttonGroups = new ButtonGroup[pNum];
        Object[][] data = new Object[pNum][2];

        for (int i = 0; i < pNum; i++) {
            String s = "" + (i + start);
            //  System.out.println("\ts=" + s);
            data[i][0] = s;
            JPanel jp = new JPanel();
            //    jp.setOpaque(true);
            jp.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            //  if (shouldFill) {
            //natural height, maximum width
            c.fill = GridBagConstraints.HORIZONTAL;
//}

            ButtonGroup gr = new ButtonGroup();
            JRadioButton[] rButtons = new JRadioButton[groupNum];
            // selectedValuesArr[i] = "A";
            for (int j = 0; j < groupNum; j++) {
                rButtons[j] = new JRadioButton(groupName[j]);
                if (j == 0) {
                    rButtons[j].setSelected(true);

                    //  rButtons[j].setBorder(null);
                    //rButtons[j].setBorderPainted(false);
                    //rButtons[j].setMargin(new Insets(2,20,2,20));
                }
                rButtons[j].addActionListener(this);
                c.gridx = j;
                c.gridy = 0;
                c.insets = new Insets(0, 10, 0, 10);
                gr.add(rButtons[j]);
                jp.add(rButtons[j], c);
                // jp.add(new JRadioButton(h[j]));
                // System.out.println("i=" + i + "\tj=" + j);
                //  data[i][j+1] =rButtons[j];

            }
            //  P[i]=jp;
            // bGroup[i] = gr;
            buttonGroups[i] = gr;
            data[i][1] = jp;
            //     System.out.println("\tdata [" + i + "]" + "[1]=" + jp.getComponentCount());
        }
        DefaultTableModel model = new DefaultTableModel(data, new Object[]{"대상자", "그룹"}) {
            public Class getColumnClass(int column) {
                if (column == 1) {
                    return JPanel.class;
                } else {
                    return String.class;
                }
            }
    public boolean isCellEditable(int row, int column) {
       //all cells false
       if (column == 0) {
                    return false;
                } 
       return true;
    }
        };

        return model;
    }

    public void makeTable(int start, int end, int groupNum) {

        table = new JTable(makeModel(start, end, groupNum)) {
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                repaint();
            }
        };

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(width0);
       // columnModel.getColumn(0)..isCellEditable(false) ;

        //   columnModel.getColumn(0).setMinWidth(20);
        columnModel.getColumn(0).setMaxWidth(width0);
        width1 = 30 * groupNum;
        // System.out.println(groupNum + "width1" + width1);
        columnModel.getColumn(1).setPreferredWidth(width1);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//        System.out.println("table row="+table.getRowCount());
//        for(int j=0; j<table.getRowCount();j++)
//       System.out.println(j+"--->"+table.getModel().getValueAt(j,0));

        TableColumn column = columnModel.getColumn(1);

        JCheckBoxRenderer renderer = new JCheckBoxRenderer(new JCheckBox());
        column.setCellRenderer(renderer);
        column.setCellEditor(renderer);
        table.setRowHeight(25);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int col) {
              

                if (col == 0) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

                    //JPanel jp= (JPanel) table.getModel().getB.getValueAt(row, 1);
                    if (!isSelected) {
                        if (row % 2 == 1) {
                            setBackground(new Color(242, 242, 242));
                        } else {
                            setBackground(Color.white);
                        }            

                    }
                }

                return this;
            }

        };

        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        //  centerRenderer.setBackground(Color.white);
        //columnModel.getColumn(0)..isEnabled(false);
        columnModel.getColumn(0).setCellRenderer(centerRenderer);

        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        header.setDefaultRenderer(headerRenderer);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        //Get the status for the current row.
//        JButton jb = new JButton("확인");
//        jb.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                //Execute when button is pressed
//                System.out.println("You clicked the button");
//                 
//                setSelectedValueArr();
//                
//            }
//
//            private void setSelectedValueArr() {
//                selectedValueArr=new String[buttonGroups.length];
//             for (int k = 0; k < buttonGroups.length; k++) {
//                 
//                        String s = getSelectedButtonText(buttonGroups[k]);
//                        System.out.println(k + "-->" + s);
//                        selectedValueArr[k]=s;
//                  }      
//            
//            }
//        });
        // JPanel jp = new JPanel();
    }

    public JPanel getRadioPanel() {

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(new JScrollPane(table));
        //     this.setOpaque(true);
        //        setBackground(Color.BLACK);  
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      //     this.add(jb);
        // getContentPane().add(jp);

        //getContentPane().add(jb);
        //   setLocationRelativeTo(null);
        //  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        // setMyPanelSize(600,600);
        return this;
    }

    public void setMyPanelSize(int x, int y) {
        this.setSize(x, y);
    }

    public void repaint() {

        if (table != null) {
            //Component c = this.getComponent(0);
            table.repaint();
        }
    }

    public ButtonGroup[] getButtonGroupsArr() {
        return buttonGroups;
    }

    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MyRadioPanel test = new MyRadioPanel();
                JFrame jf = new JFrame();
                jf.add(test.getRadioPanel());
                jf.setVisible(true);
                jf.setSize(600, 600);
                //   setLocationRelativeTo(null);
                jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //  setVisible(true);
            }
        });
    }

    public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }

    @Override// radiobutton actions
    public void actionPerformed(ActionEvent e) {

     //   System.out.println("actionPerformed\t" + e.getActionCommand());
        //  System.out.println("actionPerformed\t" + e.getSource());

   //     AbstractButton aButton = (AbstractButton) e.getSource();
      //  System.out.println("Selected: " + aButton.getText());
//        selectedValuesArr(aButton.getText());

    }

    class JCheckBoxRenderer extends DefaultCellEditor
            implements TableCellRenderer, TableCellEditor, ItemListener {

        JPanel editCell;
        JRadioButton[] buttons;

        public JCheckBoxRenderer(JCheckBox checkBox) {
            super(checkBox);
        }

        public void setSelectedIndex(int index) {
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setSelected(i == index);
  //   System.out.println("in setSelected Index----" + buttons[i].getSelectedIcon());
            }
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Integer) {
                setSelectedIndex(((Integer) value).intValue());
                //  selectedValues[row]=buttons[((Integer) value))];

            }
            if (value instanceof JPanel) {
              //  System.out.println(row + "   JPanel in getTableCellRendererComponent     " + column);
                JPanel jp = (JPanel) value;
                if (row % 2 == 1) {
                    jp.setBackground(new Color(242, 242, 242));
                } else {
                    jp.setBackground(Color.white);
                }

            }
//            if (value instanceof JComboBox) {
//                System.out.println(row + "   JComboBox in getTableCellRendererComponent     " + column);
//            }
//            if (value instanceof JRadioButton) {
//                System.out.println(row + "   JRadioButton in getTableCellRendererComponent     " + column);
//            }
            return (Component) value;
        }

        public Component getTableCellEditorComponent(//before changed//returnj values are all init values
                JTable table, Object value, boolean isSelected, int row, int column) {
            if (value instanceof JPanel) {
                editCell = (JPanel) value;
            } else if (value instanceof JLabel) {
                JPanel j = new JPanel();
                j.add((Component) value);
                editCell = j;//(JLabel) value;
            } else {
                JPanel j = new JPanel();
                j.add((Component) value);
                editCell = j;
            }

            //    System.out.println("in getTableCellEditorComponent");
//            ((JRadioButton) editCell.getComponent(0)).addItemListener(this);
//             selectedRow=row;
//            AbstractButton aButton = (AbstractButton) editCell.getComponent(0);                  
            return editCell;
        }

        public Object getCellEditorValue() {
            ((JRadioButton) editCell.getComponent(0)).removeItemListener(this);
            return editCell;
        }

        public void itemStateChanged(ItemEvent e) {
            super.fireEditingStopped();
        }
    }

}
