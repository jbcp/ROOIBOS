

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;


/**
 *showScheduleTable 을 위한 table model. MyTableModelHelper에서 생성된
 * 스케쥴을 받아서 data와 columNames 로 받는다.
 * @author Ji-hyoung Lee
 */

public class ATableModel extends DefaultTableModel{//extends AbstractTableModel {
 private String[] columnNames;
 private Object[][] data;
 public ATableModel(Object[][]data, String[]col){
    // super.(data,columnNames);
     this.data=data;
     this.columnNames=col;
     
 }
  
 public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        if(data==null) return 0;
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        if(getValueAt(0, c)==null) return null;
        return getValueAt(0, c).getClass();
    }
   
}
