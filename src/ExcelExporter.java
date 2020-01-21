import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.*;
import jxl.*;
import jxl.write.*;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class ExcelExporter {

    final JFileChooser fc = new JFileChooser();

    void fillData(JTable table, File file) throws IOException, WriteException {
      
            Date standardDate=new Date();
            int standardCol = 3;
            WritableWorkbook workbook1 = Workbook.createWorkbook(file);
              
        SimpleDateFormat  sformat = new SimpleDateFormat("yyyy-MM-dd");

        String DateToStr = sformat.format(standardDate);
              
            
            WritableSheet sheet1 = workbook1.createSheet(DateToStr, 0);
            TableModel model = table.getModel();
          
            for (int i = 0; i < model.getColumnCount(); i++) {
                Label column = new Label(i, 0, model.getColumnName(i));
                if (model.getColumnName(i).equals("기준") || model.getColumnName(i).equals("Standard")) {
                    standardCol = i;
                }
                sheet1.addCell(column);
            }
            int j = 0;
            int i=0;
            for (i = 0; i < model.getRowCount(); i++) {
                for (j = 0; j < model.getColumnCount(); j++) {

                    if (model.getValueAt(i, j) == null) {
                        continue;
                    }
                    Object v = model.getValueAt(i, j);

                    if (v instanceof Date) {
                        if (i == 0 && j == standardCol) {
                            standardDate = (Date) v;
                        }
                        WritableCellFormat format = new jxl.write.WritableCellFormat(new jxl.write.DateFormat("HH:mm"));
                        WritableCell cell = new jxl.write.DateTime(j, i + 1, (Date) v, format);
                        sheet1.addCell(cell);

                    } else {

                        String tmp = model.getValueAt(i, j).toString();
//System.out.println("(" + i + "," + j + ")  :" + tmp);
                        Label cell = new Label(j, i + 1, tmp);
                        //                 System.out.println("2222(" + i + "," + j + ")  :" + tmp);
                        sheet1.addCell(cell);
                    }
   //System.out.println("model.getRowCount()"+model.getRowCount()+"\tmodel.getColumnCount()" + model.getColumnCount());

                }
            }
                
            workbook1.write();
            workbook1.close();
   
    }

    public static void main(String[] args) {
        String[][] data = {{"Housewares", "Rx.1275.00"}, {"Housewares", "Rx.1275.00"}, {"Housewares", "Rx.1275.00"}, {"1", ""}};
        String[] headers = {"Dep", "DR"};
        JFrame frame = new JFrame("hi");
        DefaultTableModel model = new DefaultTableModel(data, headers);
        final JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        JButton export = new JButton("Export");
        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    ExcelExporter exp = new ExcelExporter();
                    String username = System.getProperty("user.name");

                    String dir = "C:" + File.separator + File.separator + "Users" + File.separator + username + File.separator + "Desktop" + File.separator + "RoooibosTable.xls";
                    exp.fillData(table, new File(dir));
                    JOptionPane.showMessageDialog(null, "RooibosTable.xls 파일이 바탕화면에 저장되었습니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        frame.getContentPane().add("Center", scroll);
        frame.getContentPane().add("South", export);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);

    }
}
