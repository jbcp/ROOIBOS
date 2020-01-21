import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Administrator
 */
public class TimeCellEditor extends DefaultCellEditor implements TableCellEditor {

    private PopupDialog popup;
    private Date currentDate;
    private JButton editorComponent;
    SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");//dd/MM/yyyy

    public TimeCellEditor() {
        super(new JTextField());
        init();
    }

    public TimeCellEditor(JTextField textField) {
        super(textField);

        init();
    }

    public void init() {
        setClickCountToStart(2);
        editorComponent = new JButton();
        editorComponent.setBackground(Color.white);
        editorComponent.setBorderPainted(false);
        editorComponent.setContentAreaFilled(false);

        // Make sure focus goes back to the table when the dialog is closed
        editorComponent.setFocusable(false);

        //  Set up the dialog where we do the actual editing
        popup = new PopupDialog();

//        popup.addWindowListener(new java.awt.event.WindowAdapter() {
//            @Override
//            public void windowClosing(java.awt.event.WindowEvent e) {
//                System.exit(0);
//            }
//        });
    }

    public Object getCellEditorValue() {
        return currentDate;
    }

    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {

        //value = sdfDate.format(value);
        if (value instanceof Date) {
            currentDate = (Date) value;
        } else {
            System.out.println("currentDate is not value date" + value.toString());
            // editorComponent.setText(currentText);
            currentDate = new Date(0);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                popup.setText((Date) value);
//              popup.setLocationRelativeTo( editorComponent );
                Point p = editorComponent.getLocationOnScreen();
                popup.setLocation(p.x, p.y + editorComponent.getSize().height);
                popup.setVisible(true);
                fireEditingStopped();
            }
        });

 // System.out.println("value=" + value + "  sdfDate.format(value)= " + sdfDate.format(value) + "  editor Component" + editorComponent.getText());
        editorComponent.setText(sdfDate.format(value));
        return editorComponent;
    }

    class PopupDialog extends JDialog {

        private JComboBox hourCB;
        private JComboBox minCB;
        private Date newStandardTime;
        int commit = 0;
        Calendar cal = Calendar.getInstance();

        private void setNewStandardTime() {

            int hour = Integer.parseInt(hourCB.getSelectedItem().toString());
            int min = Integer.parseInt(minCB.getSelectedItem().toString());
            // newStandardTime = hour * 60 + min;
            // newStandardTime=currentDate;
//  System.out.println("hourCB.getSelectedItem().toString() "+ hourCB.getSelectedItem().toString() +"  minCB.getSelectedItem().toString()  "+minCB.getSelectedItem().toString());

//System.out.println("setNewStandardTime/ selected from combobox ==> Int value = "+ hour +":"+min);
            cal.setTime(currentDate);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, min);
            newStandardTime = cal.getTime();

 // System.out.println("newStandardTime "+ cal.getTime() );
        }

        private Date getNewStandardTime() {
            return newStandardTime;
        }

        public PopupDialog() {

            super((Frame) null, "Change", true);

            hourCB = new javax.swing.JComboBox();
            hourCB.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"}));

            hourCB.setSelectedIndex(8);

            hourCB.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    setNewStandardTime();
                }
            });

            minCB = new javax.swing.JComboBox();
            minCB.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"}));
            minCB.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {

                    setNewStandardTime();
                }
            });
//// Code adding the component to the parent container - not shown here
//
//   this.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {       
//                closeDialog(true);
//                System.exit(0);
//            } //windowClosing
//        });
            JPanel aPane = new JPanel();
            aPane.add(hourCB);
            aPane.add(new JLabel(":"));
            aPane.add(minCB);
//
            JButton ok = new JButton("OK");
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {

                    doClose(true);
                }
            });

//        JButton cancel = new JButton("Cancel");
//        cancel.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent event) {
//                closeDialog(false);
//            }
//        });
            JPanel buttons = new JPanel();
            //  buttons.add(selectAll);
            buttons.add(ok);
            //   buttons.add(cancel);
//
            JPanel panel = new JPanel(new BorderLayout());
//        //    panel.setPreferredSize(new Dimension(100,150));
//        panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
            panel.add(aPane, BorderLayout.CENTER);
            panel.add(buttons, BorderLayout.SOUTH);
            setContentPane(panel);
            pack();

            getRootPane().setDefaultButton(ok);

        }

//    public int getReturnStatus() {
//        return returnStatus;
//    }
        private void doClose(boolean commit) {
            if (commit) {
                currentDate = getNewStandardTime();

            }
            setVisible(false);
            dispose();
        }
// public void closeDialog(boolean commit) {
//            if (commit) {
//                cText = ""+getNewStandardTime();
//            }
//
//          //  jlist.requestFocusInWindow();
//            setVisible(false);
//        }

        public void setText(Date date) {
            //  textArea.setText( text );
            String[] time = sdfDate.format(date).split(":");

            String hour = time[0];
            String min = time[1];
 //System.out.println("setText " + hour);
            int p = Integer.parseInt(hour);
            //System.out.println("setText " + hour+"\t int= "+p);
            hourCB.setSelectedItem(hour);
            minCB.setSelectedItem(min);
        }

    }
}
