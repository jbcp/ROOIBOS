
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Rooibos v.1.2.Kor This program is to help Clinical trial conducting on right
 * scheduled time. MyTableModelHelper get user's input data and return time
 * table. scheduler plays right wav files through player.java
 *
 * @author Ji-hyoung Lee
 */
public class Rooibos extends javax.swing.JFrame {

    private int pNum;//시험대상자 수
    private int startNum;// 첫번째로 시험 시작하는 대상자 번호
    private int callingMin;// 호출시간 : 계획된 시각 몇분 전
    private int preparingSec; // 준비시간: 계획된 시각 몇초전
    private int gapBetweenSubjects; // 같은 시험 회차(주기,period)에서 시험대상자와 그 다음 대상자와의 시간 간격
    private int startHour;//시험 시작 시각, 시
    private int startMin;//시험 시작 시각, 분
    public boolean enableCalling;  //안내호출사용_체크시 true
    public boolean enablePreparing;//준비안내사용_체크시 true
    public boolean isStopped;     //방송중단... 정지버튼을 눌렀을때 true
    private final int COLUMN_WIDTH = 35; //스케쥴 보여주는 표(showScheduleTable) column width
    private final int COLUMN_1_WIDTH = 15;//주기 설정표 가운데 열 너비 (width of ":" col )
    private TreeMap tm; //스케쥴이 저장되는 tree
    private final int callingOffset = 5;// 호출을 정확히 callinMin전이 아니라 정각에서 offset 을 준다. due to player is busy saying counting , give some offset to call 

    // private int pLimit=20;//시험 대상자의 수 아카데믹 버젼은 20...현 음성파일은 50까지 제공
    private final int pLimit = 50;//시험 대상자의 수 아카데믹 버젼은 20...현 음성파일은 50까지 제공

    public Rooibos() {
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/imgPackage/Rooibos_logo_32_white.png")));
        initComponents();
        runWatch();//두번째 카드의 시계를 시작한다.
        getNewTableModel();//빈 표를 보여줌
    }

    private void runWatch() {
        final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy. MM. dd EEE");
        final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");

        ActionListener time = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                long currentTime = System.currentTimeMillis();
                Calendar timeNow = Calendar.getInstance();
                // 콘솔에 현재 시간 출력
                timeNow.setTimeInMillis(currentTime);
                Date d = timeNow.getTime();

                dateLabel.setText(sdf1.format(d));
                timeLabel.setText(sdf2.format(d));
            }
        };
        new Timer(1, time).start();
    }

    private void clearErrorLabel() {
        setErrorMessage("");
    }

    private void getUserInput() {
        try {
            pNum = Integer.parseInt(pNumTF.getText());
            startNum = Integer.parseInt(startNumTF.getText());
            callingMin = Integer.parseInt(callingTF.getText());
            preparingSec = Integer.parseInt(preparingTF.getText());
            gapBetweenSubjects = Integer.parseInt(gapTF.getText());
            startHour = Integer.parseInt(hourCB.getSelectedItem().toString());
            startMin = Integer.parseInt(minCB.getSelectedItem().toString());
            enableCalling = enableCallingCB.isSelected();
            enablePreparing = enablePreparingCB.isSelected();
        } catch (Exception e) {
// System.out.println("error" + e.getStackTrace().toString());
            setErrorMessage("정수만 입력하여 주십시오.");
        }
    }

    /*MyTableModelHelper get 
     (int startNum, int pNum, int startTime, int callGapTime, List<Integer> periodTimeList) 
     And then, make schedules, save them, and make tableModel
     */
    private void makeTable() {
        MyTableModelHelper aHelper = new MyTableModelHelper(startNum, pNum, (startHour * 60 + startMin), gapBetweenSubjects, getPeriodTimeList());
// System.out.println("------" + aHelper.getData().length + "\t" + aHelper.getColTitleArr().length);
        setModel(aHelper.getData(), aHelper.getColTitleArr());
        tm = aHelper.getTreeMap();
    }

    private boolean checkValidate() {
        if ((startNum + pNum) > 51) {
            setErrorMessage("대상자의 번호는 " + 50 + "까지만 사용할 수 있습니다.");
            return false;
        } else if (startNum < 1) {
            setErrorMessage("시작번호는 1 이상이여야합니다.");
            return false;
        } else if (pNum < 1) {
            setErrorMessage("대상자수는 1 이상이여야합니다.");
            return false;
        } else if (gapBetweenSubjects > 10) {
            setErrorMessage("같은 회차시험에서 대상자 시험 간격은 10분 이하이여야합니다.");
            return false;
        } else if (preparingSec < 10 || preparingSec > 300) {
            setErrorMessage("준비안내는 최소 10초전에 그리고 5분 이내에 방송합니다.");
            return false;
        } else if (enableCalling && (callingMin > 10)) {//useCall;ing==null, true
            setErrorMessage("호출시간은 10분 이내이여야합니다.");
            return false;
        } else if (pNum > pLimit) {
            setErrorMessage("아카데믹 버젼은 대상자수가 " + pLimit + " 명으로 제한되었습니다.");
            return false;
        }

        return true;
    }
    
    /* Read user inputted period times and then make calculate them as minutes 
     return relative_period_in_minute_list like 30,60,90,so on..
     */
    private List<Integer> getPeriodTimeList() {
        DefaultTableModel dtm = (DefaultTableModel) periodSettingTable.getModel();
        int rows = dtm.getRowCount();//, cols = dtm.getColumnCount();
        List<Integer> periodSetList = new ArrayList<>();
        int tmp;
        for (int i = 0; i < rows; i++) {
            tmp = 0;
            Object hr = dtm.getValueAt(i, 0);
            Object min = dtm.getValueAt(i, 2);
            if (hr == null && min == null) {
                break;
            }
            if (hr != null) {               
                tmp += (int) Math.round(Double.parseDouble(hr.toString()) * 60); //it would round the value. 13.566566->14             
            }
            if (min != null) {              
                 tmp += Integer.parseInt(min.toString());               
            }
            // System.out.println("tmp"+tmp);
            periodSetList.add(tmp);            
        }
        return periodSetList;
    }

    private void setModel(String[][] data, String[] col) {
        ATableModel model = new ATableModel(data, col);

        showScheduleTable.setModel(model);
        int colIndex = showScheduleTable.getColumnCount();
        for (int i = 0; i < colIndex; i++) {
            showScheduleTable.getColumnModel().getColumn(i).setCellRenderer(new StatusRowCellRenderer(startNum));
        }
        showScheduleTable.getColumn("").setPreferredWidth(COLUMN_WIDTH);
        periodSettingTable.getColumn(":").setPreferredWidth(COLUMN_1_WIDTH);
        setTableAlignment(periodSettingTable);
        showScheduleTable.setFillsViewportHeight(true);
        showScheduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void getNewTableModel() {
        //  pNum = Integer.parseInt(pNumTF.getText());
        MyTableModelHelper aHelper = new MyTableModelHelper(0);//(startNum, pNum, (startHour * 60 + startMin), gapBetweenSubjects, getPeriodTimeList());
        setModel(aHelper.getData(), aHelper.getColTitleArr());
    }

    /*set table header text align  */
    public void setTableAlignment(JTable table) {
        // table header alignment
        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        header.setDefaultRenderer(renderer);
        renderer.setHorizontalAlignment(JLabel.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        containerjPanel = new javax.swing.JPanel();
        jTabbedPanel = new javax.swing.JTabbedPane();
        settingPanel = new javax.swing.JSplitPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        createBT = new javax.swing.JButton();
        clearBT = new javax.swing.JButton();
        stopBT = new javax.swing.JButton();
        runBT = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        pNumTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        gapTF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        startNumTF = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        preparingTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        callingTF = new javax.swing.JTextField();
        enableCallingCB = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        hourCB = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        minCB = new javax.swing.JComboBox();
        timeSetTablePanel = new javax.swing.JScrollPane();
        periodSettingTable = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        enablePreparingCB = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel21 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        timeTableJSPane = new javax.swing.JScrollPane();
        showScheduleTable = new javax.swing.JTable();
        clockPanel = new javax.swing.JPanel();
        dateLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        footerPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Rooibos");
        setBackground(new java.awt.Color(255, 255, 255));
        setFocusCycleRoot(false);
        setFocusTraversalPolicyProvider(true);
        setMinimumSize(new java.awt.Dimension(800, 500));
        setPreferredSize(new java.awt.Dimension(1024, 740));

        containerjPanel.setMinimumSize(new java.awt.Dimension(800, 600));

        jTabbedPanel.setBackground(new java.awt.Color(204, 204, 204));
        jTabbedPanel.setMinimumSize(new java.awt.Dimension(740, 600));
        jTabbedPanel.setPreferredSize(new java.awt.Dimension(1024, 600));
        jTabbedPanel.setRequestFocusEnabled(false);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel3.setPreferredSize(new java.awt.Dimension(220, 112));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        createBT.setBackground(new java.awt.Color(51, 51, 51));
        createBT.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        createBT.setForeground(new java.awt.Color(255, 255, 255));
        createBT.setText("생성");
        createBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBTActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 34, 0, 0);
        jPanel3.add(createBT, gridBagConstraints);

        clearBT.setBackground(new java.awt.Color(51, 51, 51));
        clearBT.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        clearBT.setForeground(new java.awt.Color(255, 255, 255));
        clearBT.setText("지우기");
        clearBT.setEnabled(false);
        clearBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBTActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 45, 0, 34);
        jPanel3.add(clearBT, gridBagConstraints);

        stopBT.setBackground(new java.awt.Color(51, 51, 51));
        stopBT.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        stopBT.setForeground(new java.awt.Color(255, 255, 255));
        stopBT.setText("정지");
        stopBT.setEnabled(false);
        stopBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBTActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 45, 23, 34);
        jPanel3.add(stopBT, gridBagConstraints);

        runBT.setBackground(new java.awt.Color(51, 51, 51));
        runBT.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        runBT.setForeground(new java.awt.Color(255, 255, 255));
        runBT.setText("시작");
        runBT.setEnabled(false);
        runBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runBTActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 34, 23, 0);
        jPanel3.add(runBT, gridBagConstraints);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel4.setToolTipText("");
        jPanel4.setAutoscrolls(true);
        jPanel4.setMaximumSize(null);
        jPanel4.setName(""); // NOI18N

        jLabel1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel1.setText("대상자수");
        jLabel1.setToolTipText("최대50명으로 제한합니다.");

        pNumTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pNumTF.setText("20");

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel2.setText("간격(분)");
        jLabel2.setToolTipText("동일 샘플링 시험의 다음 대상자를 부를때까지의 시간간격입니다.");

        gapTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gapTF.setText("1");

        jLabel3.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel3.setText("시작 번호");
        jLabel3.setToolTipText("대상자 시작번호 1은 항상 시험 시작 시각과 연동됩니다."); // NOI18N

        startNumTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        startNumTF.setText("1");
        startNumTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startNumTFActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel4.setText("알림방송");
        jLabel4.setToolTipText("\"준비중\"을 알립니다.");

        preparingTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        preparingTF.setText("20");

        jLabel5.setText("초전");

        jLabel6.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel6.setText("호출시각");
        jLabel6.setToolTipText("스테이션으로 호출해야하는 해당 대상자 번호를 방송합니다.");

        jLabel7.setText("분전");

        callingTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        callingTF.setText("2");
        callingTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callingTFActionPerformed(evt);
            }
        });

        enableCallingCB.setBackground(new java.awt.Color(255, 255, 255));
        enableCallingCB.setSelected(true);
        enableCallingCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableCallingCBActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel8.setText("시험 시작 시각");

        hourCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        hourCB.setSelectedIndex(8);
        hourCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hourCBActionPerformed(evt);
            }
        });

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText(":");

        minCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        minCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minCBActionPerformed(evt);
            }
        });

        timeSetTablePanel.setBorder(null);
        timeSetTablePanel.setToolTipText("2차시험부터 30차이내로 시험시간을 정해주십시오.");
        timeSetTablePanel.setAutoscrolls(true);

        periodSettingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null},
                {null, ":", null}
            },
            new String [] {
                "시", ":", "분"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class, java.lang.Object.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        periodSettingTable.setRowHeight(20);
        periodSettingTable.setShowVerticalLines(false);
        timeSetTablePanel.setViewportView(periodSettingTable);

        jLabel12.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel12.setText("시간 설정");

        jLabel9.setText("sec. prior");

        jLabel16.setText("분");

        jLabel17.setText("명");

        jLabel14.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel14.setText("준비안내");
        jLabel14.setToolTipText("계획된 시험 시간전 해당 대상자 번호를 방송합니다.");

        enablePreparingCB.setBackground(new java.awt.Color(255, 255, 255));
        enablePreparingCB.setSelected(true);
        enablePreparingCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enablePreparingCBActionPerformed(evt);
            }
        });

        jSeparator1.setBackground(new java.awt.Color(242, 242, 242));

        jSeparator2.setBackground(new java.awt.Color(242, 242, 242));

        jLabel21.setText("번");

        jLabel27.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel27.setText("샘플링 주기");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(timeSetTablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(hourCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(minCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(33, 33, 33)))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enablePreparingCB)
                    .addComponent(enableCallingCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel6))
                .addGap(30, 30, 30)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(preparingTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(callingTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7)))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gapTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(startNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel21)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addComponent(jLabel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(11, 11, 11)))
                            .addGap(137, 137, 137)))))
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {hourCB, minCB});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(startNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel17)
                    .addComponent(pNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(gapTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enablePreparingCB)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(preparingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(6, 6, 6)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableCallingCB)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(callingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(17, 17, 17)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(hourCB)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minCB))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12))
                    .addComponent(timeSetTablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {enableCallingCB, jLabel5});

        jPanel4Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel1, jLabel12, jLabel16, jLabel17, jLabel2, jLabel3, jLabel4, jLabel6, jLabel8});

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        settingPanel.setLeftComponent(jPanel8);

        timeTableJSPane.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        showScheduleTable.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        showScheduleTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        showScheduleTable.setMaximumSize(null);
        showScheduleTable.setMinimumSize(null);
        showScheduleTable.setPreferredSize(null);
        showScheduleTable.setRequestFocusEnabled(false);
        timeTableJSPane.setViewportView(showScheduleTable);

        settingPanel.setRightComponent(timeTableJSPane);

        jTabbedPanel.addTab("설 정", settingPanel);

        clockPanel.setBackground(new java.awt.Color(0, 0, 0));
        clockPanel.setFocusCycleRoot(true);
        clockPanel.setLayout(new java.awt.GridLayout(3, 0));

        dateLabel.setBackground(new java.awt.Color(0, 0, 0));
        dateLabel.setFont(new java.awt.Font("SansSerif", 0, 50)); // NOI18N
        dateLabel.setForeground(new java.awt.Color(255, 255, 51));
        dateLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dateLabel.setText("Date");
        dateLabel.setMaximumSize(new java.awt.Dimension(32546, 32546));
        clockPanel.add(dateLabel);

        timeLabel.setBackground(new java.awt.Color(0, 0, 0));
        timeLabel.setFont(new java.awt.Font("SansSerif", 0, 240)); // NOI18N
        timeLabel.setForeground(new java.awt.Color(255, 255, 51));
        timeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timeLabel.setText("Time");
        timeLabel.setMaximumSize(new java.awt.Dimension(32546, 32546));
        timeLabel.setMinimumSize(new java.awt.Dimension(323, 193));
        timeLabel.setName(""); // NOI18N
        timeLabel.setPreferredSize(new java.awt.Dimension(323, 193));
        clockPanel.add(timeLabel);

        jTabbedPanel.addTab("시 계", clockPanel);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Authors:");
        jPanel1.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(131, 226, -1, -1));

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText("Web:");
        jPanel1.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 290, -1, -1));

        jLabel24.setFont(new java.awt.Font("굴림체", 1, 14)); // NOI18N
        jLabel24.setText("김민걸 & 이지형");
        jPanel1.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 253, 337, 19));

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText("Tea Factory Team (info@teafactory.co)");
        jPanel1.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 226, 362, -1));

        jLabel26.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel26.setText("http://www.teafactory.co");
        jPanel1.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 330, 208, 19));

        jLabel11.setFont(new java.awt.Font("굴림체", 1, 14)); // NOI18N
        jLabel11.setText("이 프로그램은 전북대학교병원 임상시험 글로벌선도센터의 후원으로 제작되었습니다.");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 380, 604, 28));

        jLabel28.setFont(new java.awt.Font("Arial", 0, 36)); // NOI18N
        jLabel28.setText("Rooibos");
        jPanel1.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(281, 125, -1, -1));

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/Rooibos_logo_128.jpg"))); // NOI18N
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(129, 69, 120, 120));

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/teafactory.jpg"))); // NOI18N
        jPanel1.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 270, 250, 67));

        jTabbedPanel.addTab("루이보스는", jPanel1);

        footerPanel.setMinimumSize(new java.awt.Dimension(1024, 40));
        footerPanel.setOpaque(false);
        footerPanel.setPreferredSize(new java.awt.Dimension(1024, 40));

        errorLabel.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        errorLabel.setForeground(new java.awt.Color(166, 0, 48));

        jLabel15.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Rooibos v1.2.Kor @ Tea Factory 2015");

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/전북대병원로고.png"))); // NOI18N
        jLabel13.setAutoscrolls(true);

        javax.swing.GroupLayout footerPanelLayout = new javax.swing.GroupLayout(footerPanel);
        footerPanel.setLayout(footerPanelLayout);
        footerPanelLayout.setHorizontalGroup(
            footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );
        footerPanelLayout.setVerticalGroup(
            footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12))
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addComponent(jLabel13)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout containerjPanelLayout = new javax.swing.GroupLayout(containerjPanel);
        containerjPanel.setLayout(containerjPanelLayout);
        containerjPanelLayout.setHorizontalGroup(
            containerjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(footerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        containerjPanelLayout.setVerticalGroup(
            containerjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerjPanelLayout.createSequentialGroup()
                .addComponent(jTabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(footerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(containerjPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(containerjPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enablePreparingCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enablePreparingCBActionPerformed
        enablePreparing = enablePreparingCB.isSelected();

        //    Scheduler.MyTask.setEnablePreparing(enablePreparing);
    }//GEN-LAST:event_enablePreparingCBActionPerformed

    private void minCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_minCBActionPerformed

    private void enableCallingCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableCallingCBActionPerformed
        enableCalling = enableCallingCB.isSelected();

        //  Scheduler.MyTask.setEnableCalling(enableCalling);
    }//GEN-LAST:event_enableCallingCBActionPerformed

    private void callingTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callingTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_callingTFActionPerformed

    private void startNumTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startNumTFActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_startNumTFActionPerformed

    private void runBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runBTActionPerformed
        createBT.setEnabled(false);
        clearBT.setEnabled(false);
        stopBT.setEnabled(true);
        runBT.setEnabled(false);

        isStopped = false;
        startBroadcast();
    }//GEN-LAST:event_runBTActionPerformed

    private void stopBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBTActionPerformed
        runBT.setEnabled(true);
        clearBT.setEnabled(true);
        createBT.setEnabled(true);
        stopBT.setEnabled(false);
        isStopped = true;
    }//GEN-LAST:event_stopBTActionPerformed

    private void clearBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBTActionPerformed
        runBT.setEnabled(false);
        createBT.setEnabled(true);
        clearBT.setEnabled(false);
        stopBT.setEnabled(false);

        clearErrorLabel();
        getNewTableModel();
    }//GEN-LAST:event_clearBTActionPerformed

    private void createBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createBTActionPerformed
        clearErrorLabel();

        getUserInput();
        if (!checkValidate()) {
            return; //stop further process
        }
        makeTable();
        runBT.setEnabled(true);
        clearBT.setEnabled(true);
        createBT.setEnabled(false);
        stopBT.setEnabled(false);
    }//GEN-LAST:event_createBTActionPerformed

    private void hourCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hourCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hourCBActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {
        
        
             String dir = System.getProperty("user.dir")+"\\" ;
            
            
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Rooibos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*log file 만들기*/
        //PrintStream console=System.out;
        File file = new File(dir+"rooibos_log.txt");
        try {
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
            Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Rooibos().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField callingTF;
    private javax.swing.JButton clearBT;
    private javax.swing.JPanel clockPanel;
    private javax.swing.JPanel containerjPanel;
    private javax.swing.JButton createBT;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JCheckBox enableCallingCB;
    private javax.swing.JCheckBox enablePreparingCB;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JTextField gapTF;
    private javax.swing.JComboBox hourCB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPanel;
    private javax.swing.JComboBox minCB;
    private javax.swing.JTextField pNumTF;
    private javax.swing.JTable periodSettingTable;
    private javax.swing.JTextField preparingTF;
    private javax.swing.JButton runBT;
    private javax.swing.JSplitPane settingPanel;
    private javax.swing.JTable showScheduleTable;
    private javax.swing.JTextField startNumTF;
    private javax.swing.JButton stopBT;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JScrollPane timeSetTablePanel;
    private javax.swing.JScrollPane timeTableJSPane;
    // End of variables declaration//GEN-END:variables

    private void setErrorMessage(String s) {
        errorLabel.setText(s);
    }
 

    private void startBroadcast() {
        long currentTime = System.currentTimeMillis();

        if ((Long) tm.lastKey() < currentTime / 1000) {
            System.out.println("the test time you set are passed to call. change start time.");
            setErrorMessage("테스트 시간이 지났습니다. 시작시간을 다시 정해주세요.");
            return;
        }
        Calendar x = Calendar.getInstance();
        x.setTimeInMillis(currentTime);
        long curMillis = currentTime - (currentTime / 1000) * 1000;
        long delayMillis = 1000 - curMillis + 1; // 정시보다 1 miliseconed 를 줘서 오류를 방지한다.

        System.out.println(x.getTime() + "\tin rooibos.java   current time on 08:00:00:" + curMillis + "\tdelaytime to 08:00:01\t  " + delayMillis);

        java.util.Timer runTimer = new java.util.Timer();
        runTimer.scheduleAtFixedRate(new MyTask(), delayMillis, 1000);
    }

    private class MyTask extends TimerTask {
        private Player pr;           
        public MyTask() {//TreeMap<Long, ArrayList<Integer>> tm,eparingSec) {//MyTask(테스트시간해쉬테이블,호출사용여부,호출시간(Min), 준비중 안내방송시간(Sec)) 
            pr = new Player();
        }

        @Override
        public void run() {
            Calendar x = Calendar.getInstance();
            long now = System.currentTimeMillis();
            x.setTimeInMillis(now);
            long currentTimeSec = now / 1000;
            long preparingtimeSec = currentTimeSec + preparingSec;
            long countingtimeSec = currentTimeSec + 5;
            long callingtimeSec = currentTimeSec + (callingMin * 60 - callingOffset);

//System.out.println("___1초 check in Run====" + x.getTime() + "\t delaytime: \t" + System.currentTimeMillis());
            if ((Long) tm.lastKey() < currentTimeSec) {
                System.out.println("isDoneATask");
                this.cancel();              
                return;
            }
            if (isStopped) {
                System.out.println("doCancel");
                this.cancel();
                return;
            }

            if (enableCalling && tm.containsKey(callingtimeSec)) {//add 1 to round up because this should be check for right on minute like  xx:xx:00.
                List<Integer> list = (List<Integer>) tm.get(callingtimeSec);
                pr.playCalling(list);
                return;
            }
            if (enablePreparing && tm.containsKey(preparingtimeSec)) {//add 1 to round up because this should be check for right on minute like  xx:xx:00.
                List<Integer> list = (List<Integer>) tm.get(preparingtimeSec);
                pr.playPreparing(list);
                return;
            }
            if (tm.containsKey(countingtimeSec)) {//add 1 to round up because this should be check for right on minute like  xx:xx:00.
                pr.resetCount();
                pr.playCounting();               
            }
        }
    }
}
