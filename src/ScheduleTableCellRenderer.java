
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JCheckBox;
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

    SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");//dd/MM/yyyy
    int startNum;
    boolean[][] status;
    private static final long serialVersionUID = 1L;
    int standard_col = 3;
    JCheckBox check = new JCheckBox();

    ScheduleTableCellRenderer(int startNum) {
        super();
        this.startNum = startNum;
        boolean[][] status = new boolean[20][3];
        Arrays.fill(status, true);
    }

    ScheduleTableCellRenderer(int startNum, boolean[][] status, int standard_col) {
        super();
        this.status = status;
        this.startNum = startNum;
        this.standard_col = standard_col;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Date) {
            Date d = (Date) value;
       // System.out.println(d.getHours()+":"+d.getMinutes());
            //  Calendar c= Calendar.getInstance();
            // c.setTime(d);
            //  System.out.println("=====in setValue"+c.getTime());

      //SimpleDateFormat sdf = new SimpleDateFormat("HH:MM");
            setText((value == null) ? "" : sdfDate.format(value));
            // System.out.println("=====in setValue"+sdfDate.format(value));

        }
        else    setText((value == null) ? "" : value.toString());

      //   if(value instanceof String) System.out.println("String==="+value);
        //  setText((value == null) ? "" : value.toString());
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        row = table.convertRowIndexToModel(row);
        setHorizontalAlignment(SwingConstants.CENTER);

        Component cellComponent = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

//         if (column == 2 && isSelected && startNum>row) {
//                
////            check.setSelected(false);
////            if (value instanceof Boolean) {
////                check.setSelected((Boolean) value);
////            }
////           
////            cellComponent=check;
//             }
//        if (column == 2) {
//
//            check.setSelected(false);
//            if (value instanceof Boolean) {
//                check.setSelected((Boolean) value);
//            }
////            if (isSelected) {
////                check.setForeground(table.getSelectionForeground());
////                check.setBackground(table.getSelectionBackground());
////            } else {
////                check.setForeground(table.getForeground());
////                check.setBackground(table.getBackground());
////            }
//            cellComponent=check;
//           // return check;
//        }
        if (!isSelected) {
            if (status[row][column]) {
                if (column == standard_col) {
                    setForeground(new Color(0, 134, 179));

                } else {
                    setForeground(Color.black);
                }
                cellComponent.setFont(cellComponent.getFont().deriveFont(
                        Font.BOLD));

            } else {
                setForeground(Color.gray);
            }

        }

//            value = sdfDate.format(value);
//System.out.println(row+"%%%"+column+"*******************%%%%%%"+value);
//        }
//        else  System.out.println(row+"%%%"+column+"%%%%%%%%%%%%%%%%"+value);
        //ystem.out.println("shceduledCellRederer check ");
        return cellComponent;

    }

}
