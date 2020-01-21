
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Administrator
 */
public class MyScheduleTableModel extends AbstractTableModel {

    int standardColIndex = 3;//기준 칼럼 index=2;{col={subject,
 
    protected Object[][] data;
    private boolean[][] backUpState;
    protected String[] columnNames;
//    Class[] titleTypes = new Class[]{
//        java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
//    };

    private boolean[][] state;
    final int MAX_SUBJECT = 50;
    private int colNum;
    private List<Integer> groupTimeList;

    public MyScheduleTableModel() {
        super();
        colNum = 10;
       
     //if(english) 
       //  MyScheduleTableModelHelper_Eng helper = new MyScheduleTableModelHelper_Eng(0, colNum);
    
   MyScheduleTableModelHelper helper = new MyScheduleTableModelHelper(0, colNum);
       
        this.data = helper.getData();
        this.columnNames = helper.getColTitleArr();
        this.state = helper.getState();
        // this.backUpState=  state.clone();

//        data=helper.getData();
    }

    public int getStandardCalIndex() {
        return standardColIndex;
    }

    public MyScheduleTableModel(String[] title, Object[][] data, boolean[][] state, List<Integer> groupList, int standardColIndex) {
        this.groupTimeList = groupList;
        this.columnNames = title;
        this.standardColIndex = standardColIndex;
       
       // if(Rooibos.ENGLISH)  setColumnName("Reference", this.standardColIndex);
        setColumnName("기준", this.standardColIndex);
        colNum = title.length;

        this.data = data;
        this.state = state;
        backUpState = new boolean[data.length][columnNames.length];
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i = 0; i < numRows; i++) {

            for (int j = 0; j < numCols; j++) {
                backUpState[i][j] = this.state[i][j];
            }
        }

     
    }

    public void setColumnName(String s, int index) {
        columnNames[index] = s;
        fireTableStructureChanged();
    }

    public Class getColumnClass(int c) {
        //  return getValueAt(0, c).getClass();
        Class type = String.class;
        switch (c) {
            case 0:
                type = Integer.class;
                break;
            case 2:
                type = Boolean.class;
                break;
        }
        return type;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getRowCount() {

        if (data == null) {
            return 0;
        }
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null || data[i].toString().equals("")) {
                return i;
            }
        }
        return data.length;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        boolean canEdit = false;

        if (col == standardColIndex) {
            canEdit = true;
        } else if (col == 2) {//(getColumnName(col).equals("활성화")) {
            if (data[row][1] != null) {//if data is null means row <start 
                canEdit = true;
            }
        }
        return canEdit;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        return data[row][col];
    }

    public Object[][] getData() {
        return data;
    }

    public void setValueAt(Object value, int row, int col) {
        Calendar calendar = Calendar.getInstance();
        if (col == 2) {
            boolean tmp = (boolean) value;
            if (!tmp) {
                Arrays.fill(state[row], false);
            } else {
                for( int j=0; j<getColumnCount();j++)
                state[row][j] = backUpState[row][j];
            }
        }
        if (col == standardColIndex) {
            // int old=data[row][col] ;
            Date oldDate = (Date) data[row][col];
            Date newDate = (Date) value;

            calendar.setTime(oldDate);
            System.out.print("oldDate [" + row + " ][ " + col + "]\t" + calendar.getTime());

            long diff = newDate.getTime() - oldDate.getTime();

            for (int k = 3; k < getColumnCount(); k++) {
                if (data[row][k] == null) {
                    break;
                } else if (k == standardColIndex) {
                    continue;
                }
                oldDate = (Date) data[row][k];//선택된 셀: 기준시간
                data[row][k] = new Date(oldDate.getTime() + diff);
                fireTableCellUpdated(row, k);
            }
        }
        data[row][col] = value;
        if (value instanceof Date) {
            calendar.setTime((Date) value);
            System.out.println(" ===New Data [" + row + "][" + col + "]\t" + calendar.getTime());

        }
        fireTableCellUpdated(row, col);
    //  printDebugData();

    }

    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i = 0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j = 0; j < numCols; j++) {
                System.out.print("  " + data[i][j] + "=>" + state[i][j]);
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }

    public Object getStateAt(int row, int col) {
        return state[row][col];
    }

    public void setStateAt(boolean b, int row, int col) {
        state[row][col] = b;
    }

    public boolean[][] getState() {
        return this.state;
    }

}
