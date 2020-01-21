
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * showScheduleTable에서 시작번호보다 작은 수의 text color= gray
 *
 * @author jhlee
 */
public class ScheduleTableCellRenderer extends DefaultTableCellRenderer {

    int startNum;
    boolean[][] status;

    ScheduleTableCellRenderer(int startNum) {
        this.startNum = startNum;
        boolean[][] status = new boolean[20][3];
        Arrays.fill(status, true);
    }

    ScheduleTableCellRenderer(int startNum, boolean[][] status) {
        this.status = status;
        this.startNum = startNum;

    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        row = table.convertRowIndexToModel(row);
        setHorizontalAlignment(SwingConstants.CENTER);
        Component cellComponent = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        if (status[row][column]) {
            setForeground(Color.black);
            cellComponent.setFont(cellComponent.getFont().deriveFont(
                    Font.BOLD));

        } else {
            setForeground(Color.gray);
        }

        return cellComponent;

    }

}
