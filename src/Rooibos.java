import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import jxl.write.WriteException;
import org.sourceforge.jcalendarbutton.JCalendarButton;
import submenu.AboutDialog;
import submenu.Clock;

/**
 * Rooibos.java
 * 
 * This program play a audio file by scheduled time. 
 *
 * @author Ji-hyoung Lee (jhlee@jbcp.kr)
 * @version 2.1.Kor
 */
public final class Rooibos extends javax.swing.JFrame {

    private final int pLimit = 100;//시험 대상자의 수 아카데믹 버젼은 20...현 음성파일은 100까지 제공
    private final int COLUMN_WIDTH = 35; //스케쥴 보여주는 표(showScheduleTable) column width
    public final static boolean DEBUG=false;
    public int[] setting = new int[7];//{start, end, gap, prepare, call, standard_today_min, group}
    public final static int START = 0;//첫번째로 시험 시작하는 대상자 번호
    public final static int END = 1; // 시험 마지막 대상자 번호
    public final static int GAP = 2; // 같은 시험 회차(주기,period)에서 시험대상자와 그 다음 대상자와의 시간 간격
    public final static int PREPARE = 3; // 준비시간: 계획된 시각 몇초전
    public final static int CALL = 4;  //호출시간 : 계획된 시각 몇분 전
    public final static int GROUP = 5; //num of groups
    public final static int STANDARD_MIN_FROM_MIDNIGHT = 6; // test time in minutes from midnight on same date (midnight is 0, 1am  is 60)
    private boolean[] errorFlag = new boolean[5];//{start, end, gap, prepare, call}
    public Clock watchFrame = new Clock();
    public final static int STANDARD_COL_INDEX = 3; //유효한 주기 데이타의 수 ㅇ기준 포함 ㅇ 
    public static boolean enableCalling;  //안내호출사용_체크시 true
    public static boolean enablePreparing;//준비안내사용_체크시 true
    private Date testDate;
    
    ///////////////////////////////////////////////////////////
    public boolean isStopped; //방송중단... 정지버튼을 눌렀을때 true
    private TreeMap treemap; //스케쥴이 저장되는 tree

    private GroupSelection groupSelectDialog = null;
    private javax.swing.JTextField[] textfieldArr; //={startTF, endTF, gapTF,prepareTF, callTF, }

    MyScheduleTableModelHelper aHelper;
    public List<Integer> periodTimeList;
    public String[] selectedGroupOfSubjectArr;
    Object pTmpData[][];
    DefaultTableModel periodDtm;
    final static int COL_DATA_START = 3;
    Object[] savedSetting = new Object[10];

    public void beforeInitComponents() {
        setting[START] = 1;
        setting[END] = 20;
        setting[GROUP] = 1;
        setting[GAP] = 1;
        setting[PREPARE] = 20;
        setting[CALL] = 2;
        setting[STANDARD_MIN_FROM_MIDNIGHT] = 480;//8am = 8*60=480min
        enableCalling = true;  //안내호출사용_체크시 true
        enablePreparing = true;//준비안내사용_체크시 true
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/imgPackage/Rooibos_logo_short32.png")));
    }

    public void afterInitComponents() {
        testDate = new Date();
        setTestDate(testDate);
        lookPeriodTableCenter(); //change tab size and look of table       
        textfieldArr = new javax.swing.JTextField[]{this.startTF, this.endNumTF, this.gapTF, this.preparingTF, this.callingTF};
        pTmpData = new Object[30][5];//tmp data
        periodDtm = (DefaultTableModel) periodSettingTable.getModel();
        lookShowTableCenter();
    }

    public Rooibos() {
        beforeInitComponents();//set default value
        initComponents();
        afterInitComponents();//declair valuables  after initComponents

        // Window Listeners
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            } //windowClosing
        });
    }

    /*if there is no error, clear error label */
    private void clearErrorLabel() {
        for (int i = 0; i < errorFlag.length; i++) {
            if (errorFlag[i]) {
                return;
            }
        }
        setErrorMessage("");
    }

    /*Read hour and min combo box and set the reference time from midnight*/
    private void saveReferenceTime() {
        int hour = Integer.parseInt(hourCB.getSelectedItem().toString());
        int min = Integer.parseInt(minCB.getSelectedItem().toString());
        setting[STANDARD_MIN_FROM_MIDNIGHT] = hour * 60 + min;
    }

    private boolean isTextFieldValidate(int index) {
        int tmp = -1;

        try {
            tmp = Integer.parseInt(textfieldArr[index].getText());

        } catch (Exception e) {
            setErrorMessage("정수를 입력하여 주십시오.");
            errorFlag[index] = true;
            return false;
        }

        switch (index) {
            case START:
                if (tmp < 1) {
                    setErrorMessage("시작번호는 1 이상이여야합니다.");
                    errorFlag[index] = true;
                    return false;
                }

                break;
            case END:
                if (tmp > pLimit) {
                    setErrorMessage("대상자의 번호는 "+pLimit+  "까지만 사용할 수 있습니다.");
                    errorFlag[index] = true;
                    return false;
                }
                if (setting[START] > tmp) {
                    setErrorMessage("대상자의 시작번호는 끝번호보다 작아야합니다.");
                    errorFlag[index] = true;
                    return false;
                }

                break;

            case GAP:
                if (tmp < 1 || tmp > 10) {
                    setErrorMessage("같은 회차시험에서 대상자 시험 간격은 1분에서 10분 사이입니다.");
                    errorFlag[index] = true;
                    return false;
                }
                break;
            case CALL:
                if (enableCalling && ((tmp > 10) || (tmp < 1))) {//useCall;ing==null, true
                    setErrorMessage("호출시간은 10분 이내이여야합니다.");
                    errorFlag[index] = true;
                    return false;
                }
                break;
            case PREPARE:
                if (tmp < 10 || tmp > 300) {
                    setErrorMessage("준비안내는 최소 10초전에 그리고 5분 이내에 방송합니다.");
                    errorFlag[index] = true;
                    return false;
                }
                break;

        }
  if(index==3)  System.out.println("isTextFieldValidate save :   "+ setting[index]+"--->"+tmp);
        setting[index] = tmp;
        errorFlag[index] = false;
        clearErrorLabel();
 
        return true;
    }

    private void makeScheduleTable() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.EEE ");
        String formattedString = formatter.format(testDate);

        System.out.println("makeScheduleTable" + formattedString);
        aHelper = new MyScheduleTableModelHelper(setting, getPeriodMap(), testDate, selectedGroupOfSubjectArr);
        setting[STANDARD_COL_INDEX] = aHelper.getStandardColIndex();
        setTableModel(aHelper.getColTitleArr(), aHelper.getData(), aHelper.getState(), aHelper.getPeriodTimeList());
    }

    private boolean checkValidateBeforeCreateTable() {
        int totalSubjects = setting[END] - setting[START] + 1;
        
        /* if user click groupButton, and group selection dialog is not null. 
        */
        if (groupSelectDialog != null) {
            if (selectedGroupOfSubjectArr.length <= (setting[END])) { 
                String[] tmp = new String[setting[END] + 1];
                for (int i = 1; i < tmp.length; i++) {
                    if (i < selectedGroupOfSubjectArr.length && selectedGroupOfSubjectArr[i] != null) {
                        tmp[i] = selectedGroupOfSubjectArr[i];
                    } else {
                        tmp[i] = "A";
                    }
                }
                selectedGroupOfSubjectArr = tmp;
            }

        } else if (totalSubjects > pLimit) {
            setErrorMessage("아카데믹 버젼은 대상자수가 " + pLimit + " 명으로 제한되었습니다.");
            return false;
        }
        for (int i = 0; i < textfieldArr.length; i++) {
            if (!isTextFieldValidate(i)) {
                return false;
            }
        }
        return true;
    }

    private TreeMap<Integer, String[]> getPeriodMap() {

        TreeMap<Integer, String[]> pMap = new TreeMap();

        int rows = periodDtm.getRowCount();
        int tmp;
        int starttime = setting[STANDARD_MIN_FROM_MIDNIGHT];//startHour * 60 + startMin;

        for (int i = 0; i < rows; i++) {

            tmp = 0;
            Object hr = periodDtm.getValueAt(i, 0);
            Object min = periodDtm.getValueAt(i, 1);

            if (hr == null && min == null) {
                continue;
            }
            if (hr != null) {
                tmp += (int) Math.round(Double.parseDouble(hr.toString()) * 60); //it would round the value. 13.566566->14             
            }
            if (min != null) {
                tmp += Integer.parseInt(min.toString());
            }

            if (!pMap.containsKey(tmp + starttime)) {
                String[] sGroup = new String[2];
                if (periodDtm.getValueAt(i, 2) == null) {
                    sGroup[0] = "All";
                } else {
                    sGroup[0] = periodDtm.getValueAt(i, 2).toString();
                }
                if (periodDtm.getValueAt(i, 3) == null) {
                    sGroup[1] = "" + 0;
                } else {
                    sGroup[1] = periodDtm.getValueAt(i, 3).toString();
                }
                pMap.put((tmp + starttime), sGroup);

            }
        }
        return pMap;
    }

    private void setTableModel(String[] title, Object[][] data, boolean[][] state, List<Integer> groupList) {
        showScheduleTable.setModel(new MyScheduleTableModel(title, data, state, groupList, setting[STANDARD_COL_INDEX]));

        int colIndex = showScheduleTable.getColumnCount();
        for (int i = 0; i < colIndex; i++) {
            if (i > 2) {
                showScheduleTable.getColumnModel().getColumn(i).setCellRenderer(new ScheduleTableCellRenderer(setting[START], state, setting[STANDARD_COL_INDEX]));
            }
        }
        ((JComponent) showScheduleTable.getDefaultRenderer(Boolean.class)).setOpaque(true);
        TimeCellEditor popupEditor = new TimeCellEditor();
        showScheduleTable.getColumnModel().getColumn(setting[STANDARD_COL_INDEX]).setCellEditor(popupEditor);

        lookShowTableCenter();
    }

    private void lookShowTableCenter() {
        showScheduleTable.getColumnModel().getColumn(0).setPreferredWidth(COLUMN_WIDTH + 15);
        showScheduleTable.getColumnModel().getColumn(1).setPreferredWidth(COLUMN_WIDTH);
        showScheduleTable.getColumnModel().getColumn(2).setPreferredWidth(COLUMN_WIDTH + 15);
        showScheduleTable.setFillsViewportHeight(true);
        showScheduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        showScheduleTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        showScheduleTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        JTableHeader header = showScheduleTable.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) showScheduleTable.getTableHeader().getDefaultRenderer();
        header.setDefaultRenderer(headerRenderer);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
    
    }
    public void lookPeriodTableCenter(){
            /*set table header text align  */
        JTableHeader header2 = periodSettingTable.getTableHeader();
        DefaultTableCellRenderer HRenderer = (DefaultTableCellRenderer) periodSettingTable.getTableHeader().getDefaultRenderer();
        header2.setDefaultRenderer(HRenderer);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();

        HRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < periodSettingTable.getColumnCount(); i++) {
            periodSettingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);//
        }
       // periodSettingTable.getColumnModel().getColumn(0).setPreferredWidth(55);
          // periodSettingTable.getColumnModel().getColumn(1).setPreferredWidth(55);
  periodSettingTable.getColumnModel().getColumn(3).setPreferredWidth(80);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        excelFileChooser = new javax.swing.JFileChooser(){   @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                        super.approveSelection();
                        return;
                        case JOptionPane.NO_OPTION:
                        return;
                        case JOptionPane.CLOSED_OPTION:
                        return;
                        case JOptionPane.CANCEL_OPTION:
                        cancelSelection();
                        return;
                    }
                }
                super.approveSelection();
            }};
            containerjPanel = new javax.swing.JPanel();
            settingContainer = new javax.swing.JPanel();
            jPanel1 = new javax.swing.JPanel();
            jPanel3 = new javax.swing.JPanel();
            jPanel5 = new javax.swing.JPanel();
            label1 = new javax.swing.JLabel();
            startTF = new javax.swing.JTextField();
            jlabel11 = new javax.swing.JLabel();
            endNumTF = new javax.swing.JTextField();
            label2 = new javax.swing.JLabel();
            gapTF = new javax.swing.JTextField();
            subjectPic = new javax.swing.JLabel();
            label3 = new javax.swing.JLabel();
            jLayeredPane2 = new javax.swing.JLayeredPane();
            label4 = new javax.swing.JLabel();
            groupSettingBT = new javax.swing.JButton();
            jPanel6 = new javax.swing.JPanel();
            callingTF = new javax.swing.JTextField();
            label8 = new javax.swing.JLabel();
            enableCallingCB = new javax.swing.JCheckBox();
            enablePreparingCB = new javax.swing.JCheckBox();
            jLabel27 = new javax.swing.JLabel();
            preparingTF = new javax.swing.JTextField();
            label7 = new javax.swing.JLabel();
            jPanel7 = new javax.swing.JPanel();
            label5 = new javax.swing.JLabel();
            hourCB = new javax.swing.JComboBox();
            minCB = new javax.swing.JComboBox();
            jLabel31 = new javax.swing.JLabel();
            displayDateLabel = new javax.swing.JLabel();
            tubePic1 = new javax.swing.JLabel();
            jPanel4 = new javax.swing.JPanel();
            label6 = new javax.swing.JLabel();
            jScrollPane1 = new javax.swing.JScrollPane();
            periodSettingTable = new javax.swing.JTable();
            tubePic = new javax.swing.JLabel();
            jPanel9 = new javax.swing.JPanel();
            timeTableJSPane = new javax.swing.JScrollPane();
            showScheduleTable = new javax.swing.JTable();
            jPanel2 = new javax.swing.JPanel();
            runBT = new javax.swing.JButton();
            stopBT = new javax.swing.JButton();
            exportToExcelBTN = new javax.swing.JButton();
            jLabel2 = new javax.swing.JLabel();
            createBT = new javax.swing.JButton();
            footerPanel = new javax.swing.JPanel();
            jPanel8 = new javax.swing.JPanel();
            jLabel13 = new javax.swing.JLabel();
            errorLabel = new javax.swing.JLabel();
            jLabel15 = new javax.swing.JLabel();
            menuBar = new javax.swing.JMenuBar();
            settingMenu = new javax.swing.JMenu();
            saveMenuItem = new javax.swing.JMenuItem();
            restoreMenuItem = new javax.swing.JMenuItem();
            jSeparator1 = new javax.swing.JPopupMenu.Separator();
            clockMenuItem = new javax.swing.JMenuItem();
            syncTimejMenuItem = new javax.swing.JMenuItem();
            jSeparator2 = new javax.swing.JPopupMenu.Separator();
            aboutMenuItem = new javax.swing.JMenuItem();
            watchMenu = new javax.swing.JMenu();

            fileChooser.setFileFilter(new MyCustomTXTFilter());

            excelFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
            excelFileChooser.setFileFilter(new MyCustomEXCELFilter());

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            setTitle("Rooibos");
            setBackground(new java.awt.Color(255, 255, 255));
            setFocusCycleRoot(false);
            setFocusTraversalPolicyProvider(true);
            setMinimumSize(new java.awt.Dimension(800, 500));
            setPreferredSize(new java.awt.Dimension(1024, 740));

            containerjPanel.setMinimumSize(new java.awt.Dimension(800, 500));
            containerjPanel.setPreferredSize(new java.awt.Dimension(1024, 500));
            containerjPanel.setLayout(new javax.swing.BoxLayout(containerjPanel, javax.swing.BoxLayout.Y_AXIS));

            settingContainer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
            settingContainer.setMaximumSize(new java.awt.Dimension(32654, 65534));
            settingContainer.setName(""); // NOI18N
            settingContainer.setPreferredSize(new java.awt.Dimension(792, 539));
            settingContainer.setLayout(new javax.swing.BoxLayout(settingContainer, javax.swing.BoxLayout.Y_AXIS));

            jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));

            jPanel3.setBackground(new java.awt.Color(153, 153, 153));
            jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
            jPanel3.setMaximumSize(new java.awt.Dimension(330, 65534));
            jPanel3.setPreferredSize(new java.awt.Dimension(330, 517));
            jPanel3.setRequestFocusEnabled(false);
            jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

            jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            label1.setText("대상자");

            startTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
            startTF.setText(""+setting[START]);
            startTF.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusLost(java.awt.event.FocusEvent evt) {
                    startTFFocusLost(evt);
                }
            });

            jlabel11.setText("~");

            endNumTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
            endNumTF.setText(""+setting[END]);
            endNumTF.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusLost(java.awt.event.FocusEvent evt) {
                    endNumTFFocusLost(evt);
                }
            });

            label2.setText("간격");

            gapTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
            gapTF.setText(""+setting[GAP]);
            gapTF.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusLost(java.awt.event.FocusEvent evt) {
                    gapTFFocusLost(evt);
                }
            });

            subjectPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/aSubject_21x30.png"))); // NOI18N
            subjectPic.setVerticalAlignment(javax.swing.SwingConstants.TOP);

            label3.setText("분");

            jLayeredPane2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            label4.setText("그룹설정");

            groupSettingBT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/group_20.png"))); // NOI18N
            groupSettingBT.setBorder(null);
            groupSettingBT.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    groupSettingBTActionPerformed(evt);
                }
            });

            jLayeredPane2.setLayer(label4, javax.swing.JLayeredPane.DEFAULT_LAYER);
            jLayeredPane2.setLayer(groupSettingBT, javax.swing.JLayeredPane.DEFAULT_LAYER);

            javax.swing.GroupLayout jLayeredPane2Layout = new javax.swing.GroupLayout(jLayeredPane2);
            jLayeredPane2.setLayout(jLayeredPane2Layout);
            jLayeredPane2Layout.setHorizontalGroup(
                jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane2Layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(groupSettingBT, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
            );
            jLayeredPane2Layout.setVerticalGroup(
                jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane2Layout.createSequentialGroup()
                    .addGap(5, 5, 5)
                    .addComponent(label4)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(groupSettingBT, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(6, 6, 6))
            );

            javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
            jPanel5.setLayout(jPanel5Layout);
            jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addComponent(subjectPic)
                    .addGap(25, 25, 25)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(label2))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(gapTF, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                        .addComponent(startTF, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(jlabel11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(endNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                    .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(startTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jlabel11)
                                .addComponent(endNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label1))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(label2)
                                .addComponent(gapTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label3)))
                        .addComponent(subjectPic, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(15, 15, 15))
            );

            jPanel3.add(jPanel5);
            jPanel3.add(Box.createRigidArea(new Dimension(0,5)));

            jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            callingTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
            callingTF.setText(""+setting[CALL]);
            callingTF.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusLost(java.awt.event.FocusEvent evt) {
                    callingTFFocusLost(evt);
                }
            });
            callingTF.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    callingTFActionPerformed(evt);
                }
            });

            label8.setText("분전");

            enableCallingCB.setSelected(true);
            enableCallingCB.setText("대기 안내 방송");
            enableCallingCB.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    enableCallingCBActionPerformed(evt);
                }
            });

            enablePreparingCB.setSelected(true);
            enablePreparingCB.setText("준비 안내 방송");
            enablePreparingCB.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    enablePreparingCBActionPerformed(evt);
                }
            });

            jLabel27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/MICROPHONE_20x25.png"))); // NOI18N
            jLabel27.setVerticalAlignment(javax.swing.SwingConstants.TOP);

            preparingTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
            preparingTF.setText(""+setting[PREPARE]);
            preparingTF.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusLost(java.awt.event.FocusEvent evt) {
                    preparingTFFocusLost(evt);
                }
            });

            label7.setText("초전");

            javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
            jPanel6.setLayout(jPanel6Layout);
            jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addComponent(jLabel27)
                    .addGap(25, 25, 25)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(enableCallingCB)
                            .addGap(18, 18, 18)
                            .addComponent(callingTF, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(enablePreparingCB)
                            .addGap(18, 18, 18)
                            .addComponent(preparingTF, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(label7))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                            .addGap(2, 2, 2)
                            .addComponent(label8)))
                    .addContainerGap(57, Short.MAX_VALUE))
            );
            jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(callingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label8)
                                .addComponent(enableCallingCB))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(preparingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(enablePreparingCB, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(15, 15, 15))
            );

            jPanel3.add(jPanel6);
            jPanel3.add(Box.createRigidArea(new Dimension(0,5)));

            jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
            jPanel7.setDoubleBuffered(false);

            label5.setText("시험 기준시각");

            hourCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
            hourCB.setSelectedIndex(8);
            hourCB.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    hourCBItemStateChanged(evt);
                }
            });

            minCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
            minCB.setAutoscrolls(true);
            minCB.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    minCBItemStateChanged(evt);
                }
            });

            jLabel31.setText(":");

            displayDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
            displayDateLabel.setText("  ");

            tubePic1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/drugIcon1.png"))); // NOI18N
            tubePic1.setVerticalAlignment(javax.swing.SwingConstants.TOP);

            javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
            jPanel7.setLayout(jPanel7Layout);
            jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addGap(60, 60, 60)
                            .addComponent(hourCB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel31)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(minCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(displayDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addGap(14, 14, 14)
                            .addComponent(tubePic1)
                            .addGap(15, 15, 15)
                            .addComponent(label5)))
                    .addGap(74, 74, Short.MAX_VALUE))
            );

            jPanel7Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {hourCB, minCB});

            jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(12, 12, 12)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tubePic1))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(displayDateLabel)
                        .addComponent(minCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel31)
                        .addComponent(hourCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(20, 20, 20))
            );

            jPanel3.add(jPanel7);
            jPanel3.add(Box.createRigidArea(new Dimension(0,5)));

            jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            label6.setText("스케쥴");

            jScrollPane1.setBorder(null);
            jScrollPane1.setForeground(new java.awt.Color(255, 255, 255));

            periodSettingTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    { new Double(0.0), new Integer(0), "All", new Integer(2)},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null, null, null},
                    {null, null,  null, null}
                },
                new String [] {
                    "시",  "분", "그룹", "대기(분)"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.Double.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Integer.class
                };
                boolean[] canEdit = new boolean [] {
                    true, true, true, true
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    if(rowIndex==0 && (columnIndex==0 || columnIndex==1))
                    return false;
                    else
                    return canEdit [columnIndex];
                }
            });
            periodSettingTable.setColumnSelectionAllowed(true);
            periodSettingTable.setFillsViewportHeight(true);
            periodSettingTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
            periodSettingTable.setRowHeight(20);
            periodSettingTable.getTableHeader().setReorderingAllowed(false);
            periodSettingTable.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    periodSettingTableMousePressed(evt);
                }
            });
            periodSettingTable.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    periodSettingTableKeyTyped(evt);
                }
            });
            jScrollPane1.setViewportView(periodSettingTable);
            periodSettingTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            periodSettingTable.setValueAt(Integer.parseInt(callingTF.getText()),0, 3);
            periodSettingTable.getColumnModel().getColumn(3).setCellEditor(new MyIntegerEditor(0,10));
            periodSettingTable.getColumnModel().getColumn(2).setCellEditor(new GroupCheckListEditor(setting[GROUP]));

            tubePic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/3tubes_30.png"))); // NOI18N
            tubePic.setVerticalAlignment(javax.swing.SwingConstants.TOP);

            javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
            jPanel4.setLayout(jPanel4Layout);
            jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(13, 13, 13)
                    .addComponent(tubePic)
                    .addGap(15, 15, 15)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(label6)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(label6)
                        .addComponent(tubePic))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                    .addGap(14, 14, 14))
            );

            jPanel3.add(jPanel4);

            jPanel1.add(jPanel3);

            jPanel9.setBackground(new java.awt.Color(153, 153, 153));
            jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.Y_AXIS));

            timeTableJSPane.setBackground(jPanel9.getBackground());
            timeTableJSPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
            timeTableJSPane.setPreferredSize(new java.awt.Dimension(460, 420));
            timeTableJSPane.setRequestFocusEnabled(false);

            showScheduleTable.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
            showScheduleTable.setModel(new MyScheduleTableModel());
            showScheduleTable.setFillsViewportHeight(true);
            showScheduleTable.setGridColor(new java.awt.Color(204, 204, 204));
            showScheduleTable.setRowHeight(18);
            showScheduleTable.getTableHeader().setReorderingAllowed(false);
            timeTableJSPane.setViewportView(showScheduleTable);

            jPanel9.add(timeTableJSPane);

            jPanel2.setBackground(new java.awt.Color(255, 255, 255));
            jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
            jPanel2.setMaximumSize(new java.awt.Dimension(32767, 70));
            jPanel2.setMinimumSize(new java.awt.Dimension(0, 70));
            jPanel2.setOpaque(false);
            jPanel2.setPreferredSize(new java.awt.Dimension(592, 70));
            jPanel2.setRequestFocusEnabled(false);

            runBT.setFont(new java.awt.Font("굴림", 1, 12)); // NOI18N
            runBT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/play51png.png"))); // NOI18N
            runBT.setBorder(null);
            runBT.setEnabled(false);
            runBT.setMargin(new java.awt.Insets(2, 10, 2, 10));
            runBT.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    runBTActionPerformed(evt);
                }
            });

            stopBT.setFont(new java.awt.Font("굴림", 1, 12)); // NOI18N
            stopBT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/stop51.png"))); // NOI18N
            stopBT.setBorder(null);
            stopBT.setEnabled(false);
            stopBT.setMargin(new java.awt.Insets(2, 10, 2, 10));
            stopBT.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    stopBTActionPerformed(evt);
                }
            });

            exportToExcelBTN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/excel_20.png"))); // NOI18N
            exportToExcelBTN.setBorder(null);
            exportToExcelBTN.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
            exportToExcelBTN.setRequestFocusEnabled(false);
            exportToExcelBTN.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    exportToExcelBTNActionPerformed(evt);
                }
            });
            jPanel2.add(Box.createHorizontalGlue());
            jPanel2.add(Box.createRigidArea(new Dimension(100,0)));

            jLabel2.setText("jLabel2");

            createBT.setFont(new java.awt.Font("굴림", 1, 12)); // NOI18N
            createBT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/processing51.png"))); // NOI18N
            createBT.setBorder(null);
            createBT.setMargin(new java.awt.Insets(2, 10, 2, 10));
            createBT.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    createBTActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
            jPanel2.setLayout(jPanel2Layout);
            jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(167, 167, 167)
                            .addComponent(jLabel2))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(createBT, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                            .addGap(58, 58, 58)
                            .addComponent(runBT, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(105, 105, 105)
                    .addComponent(stopBT, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(81, 81, 81)
                    .addComponent(exportToExcelBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
            );
            jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(exportToExcelBTN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(stopBT, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(runBT, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(createBT, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jLabel2)
                    .addGap(15, 15, 15))
            );

            //jPanel2.add(Box.createRigidArea(new Dimension(20,0)));
            stopBT.getAccessibleContext().setAccessibleDescription("");
            //jPanel2.add(Box.createRigidArea(new Dimension(20,0)));
            //jPanel2.add(Box.createRigidArea(new Dimension(50,0)));

            jPanel9.add(jPanel2);

            jPanel1.add(jPanel9);

            settingContainer.add(jPanel1);

            containerjPanel.add(settingContainer);

            footerPanel.setBackground(new java.awt.Color(255, 255, 255));
            footerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 0, 0, 0));
            footerPanel.setMaximumSize(new java.awt.Dimension(33435, 50));
            footerPanel.setMinimumSize(new java.awt.Dimension(1024, 50));
            footerPanel.setPreferredSize(new java.awt.Dimension(1024, 50));

            jPanel8.setBackground(new java.awt.Color(255, 255, 255));
            jPanel8.setAlignmentX(0.0F);
            jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.LINE_AXIS));

            jLabel13.setBackground(new java.awt.Color(255, 255, 255));
            jLabel13.setForeground(new java.awt.Color(255, 255, 255));
            jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/인하대병원.jpg"))); // NOI18N
            jLabel13.setAutoscrolls(true);
            jLabel13.setMaximumSize(new java.awt.Dimension(180, 35));
            jLabel13.setMinimumSize(new java.awt.Dimension(180, 35));
            jLabel13.setName(""); // NOI18N
            jLabel13.setPreferredSize(new java.awt.Dimension(180, 35));
            jLabel13.setVerifyInputWhenFocusTarget(false);
            jPanel8.add(jLabel13);

            errorLabel.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
            errorLabel.setForeground(new java.awt.Color(166, 0, 48));
            errorLabel.setMaximumSize(new java.awt.Dimension(32985, 35));
            errorLabel.setMinimumSize(new java.awt.Dimension(300, 35));
            errorLabel.setName(""); // NOI18N
            errorLabel.setPreferredSize(new java.awt.Dimension(200, 35));
            errorLabel.setRequestFocusEnabled(false);
            jPanel8.add(errorLabel);

            jLabel15.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
            jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel15.setText("ROOIBOS v2.1.5.KOR  @ TEASOFTs ");
            jLabel15.setMaximumSize(new java.awt.Dimension(250, 16));
            jLabel15.setMinimumSize(new java.awt.Dimension(250, 16));
            jLabel15.setPreferredSize(new java.awt.Dimension(250, 16));
            jPanel8.add(jLabel15);

            javax.swing.GroupLayout footerPanelLayout = new javax.swing.GroupLayout(footerPanel);
            footerPanel.setLayout(footerPanelLayout);
            footerPanelLayout.setHorizontalGroup(
                footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, 1024, Short.MAX_VALUE)
            );
            footerPanelLayout.setVerticalGroup(
                footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            );

            containerjPanel.add(footerPanel);

            menuBar.setBackground(new java.awt.Color(102, 102, 102));
            menuBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));

            settingMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/setting222-16.png"))); // NOI18N

            saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
            saveMenuItem.setText("설정 저장");
            saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    saveMenuItemActionPerformed(evt);
                }
            });
            settingMenu.add(saveMenuItem);

            restoreMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
            restoreMenuItem.setText("설정 불러오기");
            restoreMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    restoreMenuItemActionPerformed(evt);
                }
            });
            settingMenu.add(restoreMenuItem);
            settingMenu.add(jSeparator1);

            clockMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
            clockMenuItem.setText("시계창");
            clockMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    clockMenuItemActionPerformed(evt);
                }
            });
            settingMenu.add(clockMenuItem);

            syncTimejMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
            syncTimejMenuItem.setText("서버시간 동기화");
            settingMenu.add(syncTimejMenuItem);
            syncTimejMenuItem.addMouseListener(new MouseListener() {

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // TODO Auto-generated method stub
                    syncTime();
                }
            });
            settingMenu.add(jSeparator2);

            aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK));
            aboutMenuItem.setText("ABOUT");
            aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    aboutMenuItemActionPerformed(evt);
                }
            });
            settingMenu.add(aboutMenuItem);

            menuBar.add(settingMenu);
            menuBar.add(Box.createHorizontalGlue());

            watchMenu.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
            menuBar.add(watchMenu);
            runWatchMenu(watchMenu);
            watchMenu.addMouseListener(new MouseListener() {

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // TODO Auto-generated method stub
                    syncTime();
                }
            });

            setJMenuBar(menuBar);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(containerjPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(containerjPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE)
            );

            getAccessibleContext().setAccessibleDescription("");

            pack();
        }// </editor-fold>//GEN-END:initComponents
/* check the case of changing input values before running after creating a table.  
    savedSetting[10] = 
        0~6: setting[7]
        7  : testDate
        8  : selectedGroupOfSubjectArr[]
        9 :   periodData[]
    */
    public boolean checkChanged() {
        for (int i = 0; i < setting.length; i++) {
            int tmp = (int) savedSetting[i];
            if (tmp != setting[i]) {
                return true;
            }
        }
        if ((Date) savedSetting[7] != testDate) {
            return true;
        }
        String[] tmp = (String[]) savedSetting[8];
        if (tmp.length != selectedGroupOfSubjectArr.length) {
            return true;
        }
        for (int i = setting[START]; i < setting[END]; i++) {
            if (i >= tmp.length || i > selectedGroupOfSubjectArr.length) {
                return true;
            }

            if (!tmp[i].equals(selectedGroupOfSubjectArr[i])) {
                return true;
            }
        }

        String[] saved = (String[]) savedSetting[9];

        Vector currentData = periodDtm.getDataVector();

        for (int i = 0; i < saved.length; i++) {
            String savedStr = saved[i];
            String cur = currentData.get(i).toString();
            if (!(savedStr.equals(cur))) {
                return true;
            }
        }

        return false;
    }
    private void runBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runBTActionPerformed


        MyScheduleTableModel model = (MyScheduleTableModel) showScheduleTable.getModel();
        aHelper.setDB(model.getData(), model.getState());
        treemap = aHelper.getDB();

        long currentTimeMil = System.currentTimeMillis();

        if ((Long) treemap.lastKey() < currentTimeMil / 1000) {
            //System.out.println("the test time you set are passed to call. change start time.");
            setErrorMessage("테스트 시간이 지났습니다. 시작시간을 다시 정해주세요.");
            return;
        }
        if (checkChanged()) {
            int result = JOptionPane.showConfirmDialog(this, "테이블 생성 후 설정값이 변경되었습니다. 이대로 진행하시겠습니까?.", "values are changed", JOptionPane.YES_NO_CANCEL_OPTION);

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

        }
        createBT.setEnabled(false);
        stopBT.setEnabled(true);
        runBT.setEnabled(false);
        isStopped = false;
        setErrorMessage("");

        Calendar x = Calendar.getInstance();
        x.setTimeInMillis(currentTimeMil);
        long curMillis = currentTimeMil - (currentTimeMil / 1000) * 1000;
        long delayMillis = 1000 - curMillis + 1; // 정시보다 1 miliseconed 를 줘서 오류를 방지한다.

        System.out.println("\t" + x.getTime() + "\tstart btn pushe in rooibos.java   current time mlisdecond:" + curMillis + "\tdelaytime milisecond:  " + delayMillis);

        java.util.Timer runTimer = new java.util.Timer();
        runTimer.scheduleAtFixedRate(new MyTask(), delayMillis, 1000);

    }//GEN-LAST:event_runBTActionPerformed

    private void stopBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBTActionPerformed
        runBT.setEnabled(true);
        createBT.setEnabled(true);
        stopBT.setEnabled(false);
        isStopped = true;
    }//GEN-LAST:event_stopBTActionPerformed
    private void setSelctedGroupOfSubjectArr() {
        selectedGroupOfSubjectArr = new String[setting[END] + 1];
        Arrays.fill(selectedGroupOfSubjectArr, "A");
    }
    private void createBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBTActionPerformed

        // getUserInput();
        if (!checkValidateBeforeCreateTable()) {
            return; //stop further process
        }
        if (setting[GROUP] == 1) {
            setSelctedGroupOfSubjectArr();
        }
        makeScheduleTable();

        runBT.setEnabled(true);
        stopBT.setEnabled(false);
        
        //save user input data to savedSetting[]
        for (int i = 0; i < setting.length; i++) {
            savedSetting[i] = setting[i];
        }
        savedSetting[7] = testDate;
        String str[] = new String[setting[END] + 1];
        for (int i = setting[START]; i <= setting[END]; i++) {
            str[i] = selectedGroupOfSubjectArr[i];
        }
        savedSetting[8] = str;

        Vector v1 = periodDtm.getDataVector();
        int length=periodDtm.getRowCount();
        String[] tmp = new String[length];
        for (int row = 0; row < length; row++) {
            tmp[row] = v1.get(row).toString();
        }
        savedSetting[9] = tmp;

    }//GEN-LAST:event_createBTActionPerformed
   


    private void exportToExcelBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToExcelBTNActionPerformed

        int returnVal = excelFileChooser.showSaveDialog(this);

        //excelFileChooser.setSelectedFile(file);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = excelFileChooser.getSelectedFile();
            String fileName = file.getPath();
            //    String extension = excelFileChooser.getFileFilter().getDescription();
            int offset = fileName.lastIndexOf(".");
            //  System.out.println("fileName" + "\t" + fileName);
            if (offset == -1 || (!fileName.endsWith(".xls"))) {
                //    String message = "file suffix was not specified";
                //     JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
                file = new File(file.getAbsolutePath() + ".xls");
                // System.out.println("after fileName" + "\t" + fileName);
            }

            try {
                ExcelExporter exp = new ExcelExporter();
                exp.fillData(showScheduleTable, file);

//   System.out.println(returnVal + "\t" + file.getAbsolutePath());
                //  setErrorMessage("");
            } catch (FileNotFoundException e) {
                JOptionPane.showConfirmDialog(this, "파일이 닫고 다시 시도하십시오.", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                //   setErrorMessage("파일이 열려있거나 찾을 수 없습니다.");
                System.out.println(e.toString());
            } catch (IOException | WriteException e) {
                //    setErrorMessage("오류로인해 저장할 수 없습니다.");
                JOptionPane.showConfirmDialog(this, "오류로인해 저장할 수 없습니다", "Error", JOptionPane.YES_NO_CANCEL_OPTION);

                System.out.println(e.toString());
            }
        }
    }//GEN-LAST:event_exportToExcelBTNActionPerformed

    private void calendarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCalendarButtonActionPerformed

    }//GEN-LAST:event_jCalendarButtonActionPerformed

    private void calendarButtonPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_cButtonPropertyChange
        if (evt.getNewValue() instanceof Date) {
            setTestDate((Date) evt.getNewValue());
        }
    }//GEN-LAST:event_cButtonPropertyChange

    private void groupSettingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupSettingBTActionPerformed

        if (!isTextFieldValidate(START) || !isTextFieldValidate(END)) {
            return;
        }

        if (groupSelectDialog == null) {
            groupSelectDialog = new GroupSelection(this, true, setting[START], setting[END], setting[GROUP], selectedGroupOfSubjectArr, this.groupSettingBT);
             groupSelectDialog.setVisible(true);
            if (groupSelectDialog.getReturnStatus() == 0) {
                groupSelectDialog = null;
                return;
            }
        } else {
            groupSelectDialog.repaint(setting[START], setting[END], setting[GROUP], selectedGroupOfSubjectArr);
            //   System.out.println("groupSettingBTActionPerformed\tstart="+setting[START]+"\t end="+setting[END]+"\tgroup="+ setting[GROUP]+"\t selectedGroupOfSubjectArr size="+selectedGroupOfSubjectArr.length);
             groupSelectDialog.setVisible(true);
        }
        

        if (groupSelectDialog.getReturnStatus() == 1) {//ok
            
            setting[GROUP] = groupSelectDialog.getGroupNum();
            periodSettingTable.getColumnModel().getColumn(2).setCellEditor(new GroupCheckListEditor(setting[GROUP]));
            periodSettingTable.repaint();
            selectedGroupOfSubjectArr = groupSelectDialog.getSelectedValues();

        }


    }//GEN-LAST:event_groupSettingBTActionPerformed

    private void cButtonPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_calendarButtonPropertyChange
        if (evt.getNewValue() instanceof Date) {
            setTestDate((Date) evt.getNewValue());
        }
    }//GEN-LAST:event_calendarButtonPropertyChange

    private void hourCBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hourCBItemStateChanged
        //  isChanged=true;
        saveReferenceTime();
    }//GEN-LAST:event_hourCBItemStateChanged

    private void minCBItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_minCBItemStateChanged
        saveReferenceTime();
        // isChanged=true;
    }//GEN-LAST:event_minCBItemStateChanged

    private void endNumTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_endNumTFFocusLost
        if (!isTextFieldValidate(END)) {
            endNumTF.grabFocus();
        }
    }//GEN-LAST:event_endNumTFFocusLost

    private void startTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_startTFFocusLost
        if (!isTextFieldValidate(START)) {
            startTF.grabFocus();
        }
    }//GEN-LAST:event_startTFFocusLost

    private void gapTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_gapTFFocusLost
        if (!isTextFieldValidate(GAP)) {
            gapTF.grabFocus();
        }
    }//GEN-LAST:event_gapTFFocusLost

    private void callingTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_callingTFFocusLost
        if (!isTextFieldValidate(CALL)) {
            callingTF.grabFocus();
        }
    }//GEN-LAST:event_callingTFFocusLost

    private void preparingTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_preparingTFFocusLost
        if (!isTextFieldValidate(PREPARE)) {
            preparingTF.grabFocus();
        }
    }//GEN-LAST:event_preparingTFFocusLost

    private void periodSettingTableKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_periodSettingTableKeyTyped
        JTable target = (JTable) evt.getSource();
        int row = target.getSelectedRow();
        int column = target.getSelectedColumn();
        if (column == 0 || column == 1) {
//            if (column == 0) {
//                target.setValueAt(":", row, 1);
//            }

            if (target.getValueAt(row, 2) == null) {
                target.setValueAt("All", row, 2);
            }
            if (target.getValueAt(row, 3) == null) {
                isTextFieldValidate(CALL);
                target.setValueAt(setting[CALL], row,3);
            }
        }
    }//GEN-LAST:event_periodSettingTableKeyTyped

    private void periodSettingTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_periodSettingTableMousePressed
        JTable target = (JTable) evt.getSource();
        int row = target.getSelectedRow();
        int column = target.getSelectedColumn();
        if (column == 0 || column == 1) {
            if (target.getValueAt(row, 2) == null) {
                target.setValueAt("All", row, 2);
            }
            if (target.getValueAt(row, 3) == null) {
                isTextFieldValidate(CALL);
                target.setValueAt(setting[CALL], row, 3);
            }
        }
    }//GEN-LAST:event_periodSettingTableMousePressed

    private void enableCallingCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableCallingCBActionPerformed
        enableCalling = enableCallingCB.isSelected();
    }//GEN-LAST:event_enableCallingCBActionPerformed

    private void enablePreparingCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enablePreparingCBActionPerformed
        enablePreparing = enablePreparingCB.isSelected();
    }//GEN-LAST:event_enablePreparingCBActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        //  int returnVal = fileChooser.showSaveDialog(null);
        JFileChooser saveFile = new JFileChooser() {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {

                    int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);

                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };

        saveFile.setFileFilter(new MyCustomTXTFilter());
        int returnVal = saveFile.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File file = saveFile.getSelectedFile();
            String fileName = file.getPath();
            // String extension = saveFile.getFileFilter().getDescription();
            int offset = fileName.lastIndexOf(".");
            if (offset == -1 || (!fileName.endsWith(".txt") && !fileName.endsWith(".TXT"))) {
                //    String message = "file suffix was not specified";
                //     JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
                file = new File(file.getAbsolutePath() + ".txt");
            }

            try {
                boolean result = writeSettingFile(file);

            } catch (FileNotFoundException ex) {
                JOptionPane.showConfirmDialog(this, "파일이 닫고 다시 시도하십시오.", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);

            } catch (IOException ex) {
                JOptionPane.showConfirmDialog(this, "오류로인해 저장할 수 없습니다", "Error", JOptionPane.YES_NO_CANCEL_OPTION);

                Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void restoreMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreMenuItemActionPerformed

        int returnVal = fileChooser.showOpenDialog(this);
        fileChooser.setFileFilter(new MyCustomTXTFilter());

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                readFile(file);

            } catch (MyCustomException ex) {
                JOptionPane.showConfirmDialog(this, ex.getMessage(), "Error", JOptionPane.YES_NO_CANCEL_OPTION);
                Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                JOptionPane.showConfirmDialog(this, "저장된 파일을 찾지 못하였습니다.", "Error", JOptionPane.YES_NO_CANCEL_OPTION);
                Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                JOptionPane.showConfirmDialog(this, "저장된 파일을 읽지 못하였습니다.", "Error", JOptionPane.YES_NO_CANCEL_OPTION);
                Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }//GEN-LAST:event_restoreMenuItemActionPerformed

    private void clockMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clockMenuItemActionPerformed
        watchFrame.setVisible(true);
    }//GEN-LAST:event_clockMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        AboutDialog t = new AboutDialog(new javax.swing.JFrame(), true);
        t.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void callingTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callingTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_callingTFActionPerformed

    private void jCalendarButton1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jCalendarButton1PropertyChange
        if (evt.getNewValue() instanceof Date) {
            Date newDate = (Date) evt.getNewValue();
//            if (testDate != newDate) {
//                isChanged = true;//changedFlag[6]=true;
//            }
            setTestDate(newDate);

        }
    }//GEN-LAST:event_jCalendarButton1PropertyChange
    public boolean writeSettingFile(File settingFile) throws UnsupportedEncodingException, FileNotFoundException, IOException {

        Writer writer = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String time = sdf.format(date);

        writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(settingFile), "utf-8"));
        writer.write("*ROOIBOS saved at\n"
                + "*" + time + "\n"
                + "*0:START\n"
                + "*1:END\n"
                + "*2:GAP\n"
                + "*3:PREPARE\n"
                + "*4:CALL\n"
                + "*5:GROUP_NUM\n"//setting[5]
                + "*6:STANDARD_HOUR\n"
                + "*7:STANDARD_MIN\n"
                + "*8:REPARE_FLAG(1: true 0: false)\n"
                + "*9:CALL_FLAG(1: true 0: false)\n"
                + "*10:GROUP_LIST(index: [START]~[END]  => list[subject])  size=end+1\n"
                + "*11:SAMPLE_TABLE_DATA\n"
        );
        //  if(settingStrArr[Rooibos.START]==null) reset();
        for (int i = 0; i <= GROUP; i++) {
            writer.write(i + "\t" + setting[i] + "\n");
            if(i==3)  System.out.println(i+"-writeSettingFile-->"+setting[i]);
        }

        writer.write("6\t" + hourCB.getSelectedItem() + "\n");
        writer.write("7\t" + minCB.getSelectedItem() + "\n");
        if (enablePreparing) {
            writer.write("8\t" + "1" + "\n");
        } else {
            writer.write("8\t" + "0" + "\n");
        }
        if (enableCalling) {
            writer.write("9\t" + "1" + "\n");
        } else {
            writer.write("9\t" + "0" + "\n");
        }
        if (selectedGroupOfSubjectArr == null) {

            setSelctedGroupOfSubjectArr();

        }

        String str = String.join("|", selectedGroupOfSubjectArr);
        writer.write("10\t" + str + "\n");

        periodDtm = (DefaultTableModel) periodSettingTable.getModel();

        int nRow = periodDtm.getRowCount(), nCol = periodDtm.getColumnCount();
        for (int i = 0; i < nRow; i++) {
            writer.write("11");
            for (int j = 0; j < nCol; j++) {
                periodDtm.getValueAt(i, j);
                writer.write("\t" + periodDtm.getValueAt(i, j));
            }
            writer.write("\n");
        }
        writer.write("*END\tEND");
        writer.close();
        return true;
    }
public void syncTime(){
         String hosts[] = new String[]{"0.asia.pool.ntp.org", "time2.kriss.re.kr", "2.kr.pool.ntp.org", "time-a.nist.gov"};

              TimeSyncNTPClient ntp = null;
        long result=0;
        long diff=0;
        for (String host : hosts) {
            try {

                 ntp = new TimeSyncNTPClient(host, 100);
                Thread.sleep(1000);
                //  long t1 = System.currentTimeMillis();
                long t2 = ntp.currentTimeMillis();
                long t3 = System.currentTimeMillis();

                Date d = new Date(t2);
                diff=t3-t2;
           if(Rooibos.DEBUG) {
                  Rooibos.setErrorMessage("서버에 연결됨 : "+host+
                "\n\t"+d + " :  시간차이 = " + diff+ " ms\n");
                
             }
        
                result = ntp.getTimeInfo().getMessage().getTransmitTimeStamp().getTime();
           
                if (result > 1477035840881L) { //dump date
                   break;
                }
            } catch (Exception e) {
               // System.out.println(e.getStackTrace());
            }
        }

        if (result==0) {
            JOptionPane.showConfirmDialog(null, "리스트에 있는 모든 인터넷 서버에 연결할 수 없습니다.", "에러", JOptionPane.CLOSED_OPTION);     
        }
       else {
            ntp.syncSystemTime(result);
             
        //    JOptionPane.showConfirmDialog(null, "동기화되었습니다.", "확인", JOptionPane.CLOSED_OPTION);               
        }
}
    private boolean readFile(File settingFile) throws FileNotFoundException, IOException, MyCustomException {
// System.out.println(settingFile.getAbsolutePath());

        InputStream fis = new FileInputStream(settingFile);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        if (!line.startsWith("*ROOIBOS saved at")) {
            return false;
        }
        int row = 0;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("*")) {
                continue;
            }
            if (line.length() == 0) {
                continue;
            }
            String[] arr = line.split("\t");
            if (arr.length < 2) {
                continue;
            }
            if (arr[1].startsWith("*END")) {
                break;
            }
            int tmp = -1;
            switch (arr[0]) {
                case "0"://START
                    tmp = Integer.parseInt(arr[1]);
                    setting[START] = tmp;
                    textfieldArr[START].setText(arr[1]);
                    break;
                case "1":
                    tmp = Integer.parseInt(arr[1]);
                    setting[END] = tmp;
                    textfieldArr[END].setText(arr[1]);
                    break;
                case "2":
                    tmp = Integer.parseInt(arr[1]);
                    setting[GAP] = tmp;
                    textfieldArr[GAP].setText(arr[1]);
                    break;
                case "3":
                    tmp = Integer.parseInt(arr[1]);
                    setting[PREPARE] = tmp;
                    textfieldArr[PREPARE].setText(arr[1]);
                    break;
                case "4":
                    tmp = Integer.parseInt(arr[1]);
                    setting[CALL] = tmp;
                    textfieldArr[CALL].setText(arr[1]);
                    break;
                case "5":
                    tmp = Integer.parseInt(arr[1]);
                    setting[GROUP] = tmp;
                    break;
                case "6":
                    tmp = Integer.parseInt(arr[1]);
                    setting[STANDARD_MIN_FROM_MIDNIGHT] = tmp * 60;
                    hourCB.setSelectedItem(arr[1]);
                    break;
                case "7":
                    tmp = Integer.parseInt(arr[1]);
                    setting[STANDARD_MIN_FROM_MIDNIGHT] += tmp;
                    minCB.setSelectedItem(arr[1]);
                    break;
                case "8":
                    tmp = Integer.parseInt(arr[1]);
                    enablePreparing = (tmp == 1);
                    enablePreparingCB.setSelected(enablePreparing);

                    break;
                case "9":
                    tmp = Integer.parseInt(arr[1]);
                    enableCalling = (tmp == 1);
                    enableCallingCB.setSelected(enableCalling);
                    break;
                case "10":
                    selectedGroupOfSubjectArr = arr[1].split("\\|");

                    break;
                case "11":
                    if (arr.length > 6) {
                        throw new MyCustomException("주기 정보가 손상되었습니다.");

                    }
                        // System.out.println(arr.length + "===arr   " + arr[1] + " " + arr[2] + " " + arr[5]);

                    //pTmpData[row] = Arrays.copyOfRange(arr, 1, 6);
                    for (int i = 0; i < arr.length - 1; i++) {
                        if (arr[i + 1].equals("null")) {
                            periodDtm.setValueAt(null, row, i);
                            // continue;
                        } else {
                            if(i==4){//대기(분)칼럼
                              //  System.out.println((i+1 )+arr[i + 1]);
                            periodDtm.setValueAt(  Integer.parseInt(arr[i + 1]), row, i);
                            }
                            else  periodDtm.setValueAt(  arr[i + 1], row, i);
                        }
                    }
                    row++;

                    periodSettingTable.getColumnModel().getColumn(3).setCellEditor(new GroupCheckListEditor(setting[GROUP]));

                    break;

            }
        }
        return true;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String args[]) throws IOException {

        String dir = System.getProperty("user.dir") + "\\";

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
          try {
  UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

} catch (Exception e) {
  }
//        try {
//            //UIManager.put("nimbusBlueGrey", new Color(240,240,240));
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {//Nimbus   Metal   Ocean 
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//
//                }
//            }
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(Rooibos.class
//                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>


        /*log file 만들기   */
        //PrintStream console=System.out;//if this is uncomment, it will show the result on console
           File file = new File(dir + "rooibos_log.txt");
   if(!DEBUG){     try {
            long size = file.length();
            FileOutputStream fos;
            if (size > 100000000  )//100M
            {
                fos = new FileOutputStream(file);
            } else {
                fos = new FileOutputStream(file, true);
            }
            PrintStream ps = new PrintStream(fos);
            System.setOut(ps);
            //System.setOut(console);//기존방법
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
     /* Create and display the form */
//        javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                new Rooibos().setVisible(true);
//            }
//        });
        /* Create and display the form */
        javax.swing.SwingUtilities.invokeLater(() -> {
            new Rooibos().setVisible(true);
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JTextField callingTF;
    private javax.swing.JMenuItem clockMenuItem;
    private javax.swing.JPanel containerjPanel;
    private javax.swing.JButton createBT;
    private javax.swing.JLabel displayDateLabel;
    private javax.swing.JCheckBox enableCallingCB;
    private javax.swing.JCheckBox enablePreparingCB;
    private javax.swing.JTextField endNumTF;
    private static javax.swing.JLabel errorLabel;
    private javax.swing.JFileChooser excelFileChooser;
    private javax.swing.JButton exportToExcelBTN;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JTextField gapTF;
    private javax.swing.JButton groupSettingBT;
    private javax.swing.JComboBox hourCB;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JLabel jlabel11;
    private javax.swing.JLabel label1;
    private javax.swing.JLabel label2;
    private javax.swing.JLabel label3;
    private javax.swing.JLabel label4;
    private javax.swing.JLabel label5;
    private javax.swing.JLabel label6;
    private javax.swing.JLabel label7;
    private javax.swing.JLabel label8;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JComboBox minCB;
    private javax.swing.JTable periodSettingTable;
    private javax.swing.JTextField preparingTF;
    private javax.swing.JMenuItem restoreMenuItem;
    private javax.swing.JButton runBT;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JPanel settingContainer;
    private javax.swing.JMenu settingMenu;
    private javax.swing.JTable showScheduleTable;
    private javax.swing.JTextField startTF;
    private javax.swing.JButton stopBT;
    private javax.swing.JLabel subjectPic;
    private javax.swing.JMenuItem syncTimejMenuItem;
    private javax.swing.JScrollPane timeTableJSPane;
    private javax.swing.JLabel tubePic;
    private javax.swing.JLabel tubePic1;
    private javax.swing.JMenu watchMenu;
    // End of variables declaration//GEN-END:variables

    public void setTestDate(Date date) {

        Calendar cal = Calendar.getInstance();
        if (date == null) {
            cal.setTimeInMillis(System.currentTimeMillis());
        } else {
            cal.setTime(date);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        testDate.setTime(cal.getTimeInMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.EEE ");
        String formattedString = formatter.format(testDate);
        displayDateLabel.setText(formattedString);
    }

    public static void setErrorMessage(String s) {
        errorLabel.setText(s);
    }

    private class MyTask extends TimerTask {

        final String dir = System.getProperty("user.dir") + "\\sounds\\";
        private Player pr;

        public MyTask() {//TreeMap<Long, ArrayList<Integer>> treemap,eparingSec) {//MyTask(테스트시간해쉬테이블,호출사용여부,호출시간(Min), 준비중 안내방송시간(Sec)) 
            pr = new Player();
        }

        @Override
        public void run() {
            Calendar x = Calendar.getInstance();

            long now = System.currentTimeMillis();
            x.setTimeInMillis(now);
            long currentTimeSec = now / 1000;

            if ((Long) treemap.lastKey() < currentTimeSec) {
                System.out.println("isDoneATask");
                createBT.setEnabled(true);
                stopBT.setEnabled(false);
                runBT.setEnabled(false);
                isStopped = false;
                setErrorMessage("테스트가 끝났습니다.");
                this.cancel();
                return;
            }
            if (isStopped) {
                System.out.println("doCancel");
                setErrorMessage("테스트가 취소되었습니다.");
                this.cancel();
                return;
            }
            if (treemap.containsKey(currentTimeSec)) {
                List<MyScheduleTableModelHelper.ASchedule> list = (List<MyScheduleTableModelHelper.ASchedule>) treemap.get(currentTimeSec);
                List<Integer> preparelist = new ArrayList<>();
                List<Integer> callList = new ArrayList<>();

//System.out.println("======Rooibos==/_____1초 check in Run====" + x.getTime()+"\tcurrentTimeSec="+currentTimeSec  +"\t delay "+delayTime1);
                for (MyScheduleTableModelHelper.ASchedule list1 : list) {
                    if (list1.getType() == MyScheduleTableModelHelper.TYPE_COUNT) {
                        String str;
                        if (list1.getSubject() == 0) {
                            str = "00S.wav";
                        } else {
                            str = "" + list1.getSubject() + "S.wav";
                        }
                        if (enablePreparing) {
                            try {
                                pr.playFile(new File(dir + str));
                                return;
                            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
                                Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } else if (list1.getType() == MyScheduleTableModelHelper.TYPE_PREPARE) {
                        preparelist.add(list1.getSubject());
                    } else if (list1.getType() == MyScheduleTableModelHelper.TYPE_CALL) {
                        callList.add(list1.getSubject());
                    }
                }
                if (enablePreparing && (preparelist.size() > 0)) {
                    pr.playPreparing(preparelist);
                } else if (enableCalling && (callList.size() > 0)) {//add 1 to round up because this should be check for right on minute like  xx:xx:00.
                    pr.playCalling(callList);
                }

            }

        }

    }

    private void runWatchMenu(JMenu menu) {
        final SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");

        ActionListener time = (ActionEvent evt) -> {
            long currentTime = System.currentTimeMillis();
            Calendar timeNow = Calendar.getInstance();
            // 콘솔에 현재 시간 출력
            timeNow.setTimeInMillis(currentTime);
            Date d = timeNow.getTime();

            menu.setText(sdf1.format(d));
        };
        new Timer(1, time).start();

    }

}

class MyCustomTXTFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File file) {
        // Allow only directories, or files with ".txt" extension
        //  return file.isDirectory() || file.getAbsolutePath().endsWith(".txt");
        return file.isDirectory() || file.getName().toUpperCase().endsWith(".TXT");
    }

    @Override
    public String getDescription() {
        // This description will be displayed in the dialog,
        // hard-coded = ugly, should be done via I18N
        return "Text documents (*.txt)";
    }
}

class MyCustomEXCELFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File file) {
        // Allow only directories, or files with ".txt" extension
        return file.isDirectory() || file.getAbsolutePath().endsWith(".xls");
        // return file.getName().toUpperCase().equals(".DOC");
    }

    @Override
    public String getDescription() {
        // This description will be displayed in the dialog,
        // hard-coded = ugly, should be done via I18N
        return "Excel documents (*.xls)";
    }
}

/**
 * My custom exception class.
 */
class MyCustomException extends Exception {

    public MyCustomException(String message) {
        super(message);
    }
}
