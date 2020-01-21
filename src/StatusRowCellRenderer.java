
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * showScheduleTable에서 시작번호보다 작은 수의 text color= gray 
 * @author jhlee
 */
public class StatusRowCellRenderer extends DefaultTableCellRenderer {

    int startNum;

    StatusRowCellRenderer(int startNum) {
        this.startNum = startNum;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        row = table.convertRowIndexToModel(row);
        if (row < startNum - 1) {
            setForeground(Color.gray);
        } else {
            setForeground(Color.black);
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    }

}
