
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class MyRadioTableCellEditor extends DefaultCellEditor
            implements TableCellRenderer, TableCellEditor, ItemListener {

        JPanel editCell;
        JRadioButton[] buttons;

        public MyRadioTableCellEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        public void setSelectedIndex(int index) {
              System.out.println("in setSelected Index----" + buttons.length);

            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setSelected(i == index);
            }
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Integer) {
                setSelectedIndex(((Integer) value).intValue());
              //  selectedValues[row]=buttons[((Integer) value))];
                    return (Component) value;
            }            
            else if (value instanceof JPanel) {
                
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
