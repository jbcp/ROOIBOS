package newpackage;


import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class MyRadioFrame extends JFrame implements ActionListener {

    final String[] groupName = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    private String[] selectedValueArr;
    private int width0 = 55;
    private int width1;
    private ButtonGroup[] buttonGroups;

    public MyRadioFrame() {
        makeTable(1, 20, 1);
        JPanel jp = new JPanel();

        jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
        jp.add(new JScrollPane(table));
        JButton save = new JButton("저장");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                setSelectedValueArr();

            }

            private void setSelectedValueArr() {
                selectedValueArr = new String[buttonGroups.length];
                for (int k = 0; k < buttonGroups.length; k++) {

                    String s = getSelectedButtonText(buttonGroups[k]);
                    //   System.out.println(k + "-->" + s);
                    selectedValueArr[k] = s;
                }

            }
        });
        jp.add(save);
        jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //     this.add(jb);
        getContentPane().add(jp);

        setLocationRelativeTo(null);
        // setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        setVisible(true);
        setMyPanelSize(600, 600);
        // setRadioPanel(1,20,1);
    }

    public String[] getSelectedValueArr() {
          selectedValueArr = new String[buttonGroups.length];
                for (int k = 0; k < buttonGroups.length; k++) {

                    String s = getSelectedButtonText(buttonGroups[k]);
                    //   System.out.println(k + "-->" + s);
                    selectedValueArr[k] = s;
                }
        if (selectedValueArr == null) {
            selectedValueArr = new String[]{"A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A"};
        }
        System.out.println("selectedValueArr length= " + selectedValueArr.length);
        return selectedValueArr;
    }

    public MyRadioFrame(int start, int end, int groupNum, Component c, boolean isContinue) {

        new MyRadioFrame(start, end, groupNum, c);
        setRadioValue();

    }

    public MyRadioFrame(int start, int end, int groupNum, Component c) {

        makeTable(start, end, groupNum);   // setRadioPanel(start, end, groupNum);
        JPanel jp = new JPanel();

        jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
        jp.add(new JScrollPane(table));
        JButton save = new JButton("저장");
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                setSelectedValueArr();

            }

            private void setSelectedValueArr() {
                selectedValueArr = new String[buttonGroups.length];
                for (int k = 0; k < buttonGroups.length; k++) {

                    String s = getSelectedButtonText(buttonGroups[k]);
                    //   System.out.println(k + "-->" + s);
                    selectedValueArr[k] = s;
                }

            }
        });
        jp.add(save);
        jp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        //     this.add(jb);
        getContentPane().add(jp);

        setLocationRelativeTo(c);
        // setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        setVisible(true);
        setMyPanelSize(600, 600);
        //return jp;

//               this.setVisible(true);
//               this.setSize(600, 600);
//                //   setLocationRelativeTo(null);
//               this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
    JTable table;

    public DefaultTableModel makeModel(int start, int end, int groupNum) {
        width1 = 35 * groupNum;
        // int start = 1;
        //  int end = 20;
        int pNum = end - start + 1;
       // int groupNum = 5;
        //  System.out.println(start + "\t" + end + "\tpNum=" + pNum);

        //   JRadioButton[][] B = {{new JRadioButton()}, {new JRadioButton()}};
        //  ButtonGroup  gr = new ButtonGroup();
        //gr.add(B[0][0]); gr.add(B[1][0]);
        //String[] h = {"A", "B", "C", "D", "F"};
        // selectedValuesArr = new String[pNum];     //JPanel[] P = new JPanel[pNum];//{{new JPanel()}, {new JPanel()}};
        //P[0][0].add(B[0][0]); 
        //P[1][0].add(B[1][0]);
        buttonGroups = new ButtonGroup[pNum];
        Object[][] data = new Object[pNum][2];

        for (int i = 0; i < pNum; i++) {
            String s = "" + (i + start);
            //   System.out.println("\ts=" + s);
            data[i][0] = s;
            JPanel jp = new JPanel();
            ButtonGroup gr = new ButtonGroup();
            JRadioButton[] rButtons = new JRadioButton[groupNum];
            // selectedValuesArr[i] = "A";
            for (int j = 0; j < groupNum; j++) {
                rButtons[j] = new JRadioButton(groupName[j]);
                if (j == 0) {
                    rButtons[j].setSelected(true);
                }
                rButtons[j].addActionListener(this);

                gr.add(rButtons[j]);
                jp.add(rButtons[j]);
                // jp.add(new JRadioButton(h[j]));
                // System.out.println("i=" + i + "\tj=" + j);
                //  data[i][j+1] =rButtons[j];

            }
            //  P[i]=jp;
            // bGroup[i] = gr;
            buttonGroups[i] = gr;
            data[i][1] = jp;
            //      System.out.println("\tdata [" + i + "]" + "[1]=" + jp.getComponentCount());
        }
        DefaultTableModel model = new DefaultTableModel(data, new Object[]{"대상자", "그룹"}) {
            public Class getColumnClass(int column) {
                if (column == 1) {
                    return JPanel.class;
                } else {
                    return String.class;
                }
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
        //   columnModel.getColumn(0).setMinWidth(20);
        columnModel.getColumn(0).setMaxWidth(width0);
        width1 = 30 * groupNum;
        //  System.out.println(groupNum + "width1" + width1);
        columnModel.getColumn(1).setPreferredWidth(width1);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//        System.out.println("table row="+table.getRowCount());
//        for(int j=0; j<table.getRowCount();j++)
//       System.out.println(j+"--->"+table.getModel().getValueAt(j,0));

        TableColumn column = columnModel.getColumn(1);

        TestRenderer renderer = new TestRenderer(new JCheckBox());
        column.setCellRenderer(renderer);
        column.setCellEditor(renderer);
        table.setRowHeight(25);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        columnModel.getColumn(0).setCellRenderer(centerRenderer);

        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        header.setDefaultRenderer(headerRenderer);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

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

//    public JPanel getRadioPanel() {
//       
//    }
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
                MyRadioFrame test = new MyRadioFrame(1, 20, 1, null);
               // JFrame jf = new JFrame();

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

//        System.out.println("actionPerformed\t" + e.getActionCommand());
//        //  System.out.println("actionPerformed\t" + e.getSource());
//
//        AbstractButton aButton = (AbstractButton) e.getSource();
//        System.out.println("Selected: " + aButton.getText());
//        selectedValuesArr(aButton.getText());
    }

    private void setRadioValue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    class TestRenderer extends DefaultCellEditor
            implements TableCellRenderer, TableCellEditor, ItemListener {

        JPanel editCell;
        JRadioButton[] buttons;

        public TestRenderer(JCheckBox checkBox) {
            super(checkBox);
        }

        public void setSelectedIndex(int index) {
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setSelected(i == index);
                System.out.println("in setSelected Index----" + buttons[i].getSelectedIcon());
            }
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Integer) {
                setSelectedIndex(((Integer) value).intValue());
                //  selectedValues[row]=buttons[((Integer) value))];

            }

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
