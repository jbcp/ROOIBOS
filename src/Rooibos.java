
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;


/**
 * Rooibos v.1.2.Kor This program is to help Clinical trial conducting on right
 * scheduled time. MyTableModelHelper get user's input data and return time
 * table. scheduler plays right wav files through player.java
 *
 * @author Ji-hyoung Lee
 */
public class Rooibos extends javax.swing.JFrame {

    //  private int pNum;//시험대상자 수
    private int start;// 첫번째로 시험 시작하는 대상자 번호
    private int end;//시험 마지막 대상자 번호
    private int callingMin;// 호출시간 : 계획된 시각 몇분 전
    private int preparingSec; // 준비시간: 계획된 시각 몇초전
    private int gapBetweenSubjects; // 같은 시험 회차(주기,period)에서 시험대상자와 그 다음 대상자와의 시간 간격
    private int startHour;//시험 시작 시각, 시
    private int startMin;//시험 시작 시각, 분
    public boolean enableCalling;  //안내호출사용_체크시 true
    public boolean enablePreparing;//준비안내사용_체크시 true
    public boolean isStopped;     //방송중단... 정지버튼을 눌렀을때 true
    private final int COLUMN_WIDTH = 35; //스케쥴 보여주는 표(showScheduleTable) column width
    // private final int COLUMN_1_WIDTH = 15;//주기 설정표 가운데 열 너비 (width of ":" col )
    private TreeMap treemap; //스케쥴이 저장되는 tree
    private final int callingOffset = 5;// 호출을 정확히 callinMin전이 아니라 정각에서 offset 을 준다. due to player is busy saying counting , give some offset to call 
    private Date testDate;
    // private int pLimit=20;//시험 대상자의 수 아카데믹 버젼은 20...현 음성파일은 50까지 제공
    private final int pLimit = 50;//시험 대상자의 수 아카데믹 버젼은 20...현 음성파일은 50까지 제공
//p/rivate JCalendarButton JCbutton;
   // public  final String[] groupName = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
    private int groupNum;
   //private MyRadioTable rTable;
     MyRadioPanel mrp;
  // MyRadioPanel  mrp;
   
   
    public Rooibos() {
       
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/imgPackage/Rooibos_logo_32_white.png")));
      
        testDate=new Date(); 
        groupNum=1;
      //  rTable=new MyRadioTable();        
      start=1;
      end=20;
      // mrp=new MyRadioPanel(start,end,groupNum);

      
        initComponents();
        mrp=new MyRadioPanel();
        settingPanel.setRightComponent(mrp.getRadioPanel());
        setTestDate(testDate);
        

//        calendarButton.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
//            public void propertyChange(java.beans.PropertyChangeEvent evt) {
//                datePopupChanged(evt);
//                
//            }
//        });
        // JCbutton= new JCalendarButton();
        runWatch();//두번째 카드의 시계를 시작한다.
        getNewTableModel();//빈 표를 보여줌
           
        // Window Listeners
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            } //windowClosing
        });

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
//private boolean isGroupingSettingChanged(){
//    //        try{
//            int endtmp = Integer.parseInt(endNumTF.getText());
//            // pNum=endNum-startNum+1;
//            int starttmp = Integer.parseInt(startNumTF.getText());
//            int   groupNumtmp=Integer.parseInt((String) groupNumComboBox.getSelectedItem()); 
//            
//            if((groupNum==groupNumtmp)||(endtmp==end)||(starttmp==start)){
//              return false;
//            }       
//            }
//    
//            
//          return true;  
//}
int endtmp=20;
int starttmp=1;
int groupNumtmp=1;
    private void getUserInput() {
        try {
            endtmp = Integer.parseInt(endNumTF.getText());
            // pNum=endNum-startNum+1;
             starttmp = Integer.parseInt(startNumTF.getText());
            //groupNumtmp=Integer.parseInt((String) groupNumComboBox.getSelectedItem()); 
         
            callingMin = Integer.parseInt(callingTF.getText());
            preparingSec = Integer.parseInt(preparingTF.getText());
            gapBetweenSubjects = Integer.parseInt(gapTF.getText());
            startHour = Integer.parseInt(hourCB.getSelectedItem().toString());
            startMin = Integer.parseInt(minCB.getSelectedItem().toString());
            enableCalling = enableCallingCB.isSelected();
            enablePreparing = enablePreparingCB.isSelected();
        } catch (Exception e) {
            setErrorMessage("정수만 입력하여 주십시오.");
        }
    }

    /*MyTableModelHelper get 
     (int startNum, int pNum, int startTime, int callGapTime, List<Integer> periodTimeList) 
     And then, make schedules, save them, and make tableModel
     */
    
    
    
       String[] groupSelectionArr;
    private void makeTable() {
    //  if(mrp==null)mrp=new MyRadioPanel();
       //groupSelectionArr=;
   
        MyScheduleTableModelHelper aHelper = new MyScheduleTableModelHelper(start, end, gapBetweenSubjects, getPeriodMap(), testDate,mrp.getSelectedValues());

        // MyTableModelHelper aHelper = new MyTableModelHelper(startNum, pNum, (startHour * 60 + startMin), gapBetweenSubjects, getPeriodTimeList());
        setModel(aHelper.getData(), aHelper.getColTitleArr(), aHelper.getStatus());
        treemap = aHelper.getTreeMap();
    }

    private boolean checkValidate() {
        int totalSubjects = end - start + 1;
        if(groupNum==groupNumtmp&& groupNumtmp==1){
                end = endtmp;
            // pNum=endNum-startNum+1;
              start = starttmp;
        }
        else if((groupNum!=groupNumtmp)||(endtmp!=end)||(starttmp!=start)){
             setErrorMessage("지정된 그룹세팅값이 변경되었습니다. 다시 그룹버튼을 눌러 세팅해주세요.");
            return false;
        }
        else{
              end = endtmp;
            // pNum=endNum-startNum+1;
              start = starttmp;
             // groupNum=groupNumtmp; 
        }
        
        
        if (end > 50) {
            setErrorMessage("대상자의 번호는 " + 50 + "까지만 사용할 수 있습니다.");
            return false;
        } else if (start < 1) {
            setErrorMessage("시작번호는 1 이상이여야합니다.");
            return false;
        } else if (start > end) {
            setErrorMessage("대상자의 시작번호는 끝번호보다 작아야합니다.");
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
        } else if (totalSubjects > pLimit) {
            setErrorMessage("아카데믹 버젼은 대상자수가 " + pLimit + " 명으로 제한되었습니다.");
            return false;
        }

        return true;
    }

    /* Read user inputted period times and then make calculate them as minutes 
     return relative_period_in_minute_list like 30,60,90,so on..
     */
      //List<String> periodGroupList = new ArrayList<>();
    
    private  TreeMap<Integer, String> getPeriodMap() {
        TreeMap<Integer, String> pMap=new TreeMap();
        DefaultTableModel dtm = (DefaultTableModel) periodSettingTable.getModel();
        int rows = dtm.getRowCount();//, cols = dtm.getColumnCount();
     //   List<Integer> periodSetList = new ArrayList<>();
        int tmp;
        int starttime = startHour * 60 + startMin;
       // periodSetList.add(starttime);

        for (int i = 0; i < rows; i++) {
         
            tmp = 0;
            Object hr = dtm.getValueAt(i, 0);
            Object min = dtm.getValueAt(i, 2);
           
            if (hr == null && min == null) {               
                continue;
            }
            if (hr != null) {
                tmp += (int) Math.round(Double.parseDouble(hr.toString()) * 60); //it would round the value. 13.566566->14             
            }
            if (min != null) {
                tmp += Integer.parseInt(min.toString());
            }

         //   if (!periodSetList.contains(tmp + starttime)) {
       //         periodSetList.add(tmp + starttime);
            if(!pMap.containsKey(tmp + starttime)){
                  String sGroup = dtm.getValueAt(i, 3).toString();
                  pMap.put((tmp + starttime),sGroup);
                  //periodGroupList.add(sGroup);                
            }
            
        }
 
        return pMap;
    }
//    public List getPeriodGroupList(){
//        return this.periodGroupList;
//    }
    private void setModel(String[][] data, String[] col, boolean[][] status) {
        //ATableModel model = new ATableModel(data, col);

        showScheduleTable.setModel(new DefaultTableModel(data,col));//model);
        int colIndex = showScheduleTable.getColumnCount();
        for (int i = 0; i < colIndex; i++) {
            showScheduleTable.getColumnModel().getColumn(i).setCellRenderer(new ScheduleTableCellRenderer(start, status));
        }
    // showScheduleTable.getColumnClass(0)..setPreferredWidth(COLUMN_WIDTH);
      showScheduleTable.getColumnModel().getColumn(0).setPreferredWidth(COLUMN_WIDTH+20);
        showScheduleTable.getColumnModel().getColumn(1).setPreferredWidth(COLUMN_WIDTH);
   //     periodSettingTable.getColumn(":").setPreferredWidth(COLUMN_1_WIDTH);
//        periodSettingTable.getColumn("시").setPreferredWidth(COLUMN_1_WIDTH*3);
//        periodSettingTable.getColumn("분").setPreferredWidth(COLUMN_1_WIDTH*3);
//        periodSettingTable.getColumn("선택").setPreferredWidth(COLUMN_1_WIDTH*2);
       
     //   TableColumn column = periodSettingTable.getColumnModel().getColumn(3);
//DefaultTableCellRenderer centerDatashowRenderer = showScheduleTable.getD.getDefaultRenderer();//new DefaultTableCellRenderer();
//centerDatashowRenderer.setHorizontalAlignment( JLabel.CENTER );
//showScheduleTable.setDefaultRenderer(String.class, centerDatashowRenderer);

        showScheduleTable.setFillsViewportHeight(true);
    
        showScheduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        
//        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
//        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
//        for (int i=0;i<showScheduleTable.getColumnCount();i++)
//      showScheduleTable.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );//
//        showScheduleTable.setDefaultRenderer(String.class, centerRenderer);
//((DefaultTableCellRenderer)showScheduleTable.getDefaultRenderer(String.class)).setHorizonta‌​lAlignment(SwingConstants.CENTER);
        
        
        JTableHeader header = showScheduleTable.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) showScheduleTable.getTableHeader().getDefaultRenderer();
        header.setDefaultRenderer(headerRenderer);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);        
    }

    private void getNewTableModel() {
        //  pNum = Integer.parseInt(pNumTF.getText());
        MyScheduleTableModelHelper aHelper = new MyScheduleTableModelHelper(0);//(startNum, pNum, (startHour * 60 + startMin), gapBetweenSubjects, getPeriodTimeList());
        setModel(aHelper.getData(), aHelper.getColTitleArr(),null);
    }

    /*set table header text align  */
//    public void setTableAlignment(JTable table) {
//        // table header alignment
//        JTableHeader header = table.getTableHeader();
//        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
//        header.setDefaultRenderer(renderer);
//        renderer.setHorizontalAlignment(JLabel.CENTER);
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        rightsettingPanel = new MyRadioPanel();
        containerjPanel = new javax.swing.JPanel();
        jTabbedPanel = new javax.swing.JTabbedPane();
        settingContainer = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        settingPanel = new javax.swing.JSplitPane();
        leftsettingPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        groupNumComboBox = new javax.swing.JComboBox();
        startNumTF = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        groupSettingBT = new javax.swing.JButton();
        endNumTF = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        hourCB = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        minCB = new javax.swing.JComboBox();
        timeSetTablePanel = new javax.swing.JScrollPane();
        periodSettingTable = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        gapTF = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        displayDateLabel = new javax.swing.JLabel();
        cButton = new net.sourceforge.jcalendarbutton.JCalendarButton();
        jLabel8 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        enablePreparingCB = new javax.swing.JCheckBox();
        preparingTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        callingTF = new javax.swing.JTextField();
        enableCallingCB = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        createBT = new javax.swing.JButton();
        clearBT = new javax.swing.JButton();
        stopBT = new javax.swing.JButton();
        runBT = new javax.swing.JButton();
        tablePanel = new javax.swing.JPanel();
        timeTableJSPane = new javax.swing.JScrollPane();
        showScheduleTable = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        exportToExcelBTN = new javax.swing.JButton();
        clockPanel = new javax.swing.JPanel();
        dateLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        aboutPanel = new javax.swing.JPanel();
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
        jLabel13 = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();

        javax.swing.GroupLayout rightsettingPanelLayout = new javax.swing.GroupLayout(rightsettingPanel);
        rightsettingPanel.setLayout(rightsettingPanelLayout);
        rightsettingPanelLayout.setHorizontalGroup(
            rightsettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 593, Short.MAX_VALUE)
        );
        rightsettingPanelLayout.setVerticalGroup(
            rightsettingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 592, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Rooibos");
        setBackground(new java.awt.Color(255, 255, 255));
        setFocusCycleRoot(false);
        setFocusTraversalPolicyProvider(true);
        setMinimumSize(new java.awt.Dimension(800, 500));
        setPreferredSize(new java.awt.Dimension(1024, 740));

        containerjPanel.setMinimumSize(new java.awt.Dimension(800, 600));

        jTabbedPanel.setBackground(new java.awt.Color(204, 204, 204));
        jTabbedPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        jTabbedPanel.setForeground(new java.awt.Color(255, 255, 255));
        jTabbedPanel.setMinimumSize(new java.awt.Dimension(740, 600));
        jTabbedPanel.setPreferredSize(new java.awt.Dimension(1024, 600));
        jTabbedPanel.setRequestFocusEnabled(false);

        settingContainer.setLayout(new javax.swing.BoxLayout(settingContainer, javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        settingPanel.setName(""); // NOI18N
        settingPanel.setPreferredSize(new java.awt.Dimension(1000, 454));
        settingPanel.setVerifyInputWhenFocusTarget(false);

        leftsettingPanel.setToolTipText("");
        leftsettingPanel.setAutoscrolls(true);
        leftsettingPanel.setMinimumSize(new java.awt.Dimension(400, 0));
        leftsettingPanel.setName(""); // NOI18N
        leftsettingPanel.setPreferredSize(new java.awt.Dimension(400, 0));
        leftsettingPanel.setLayout(new javax.swing.BoxLayout(leftsettingPanel, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "대상자", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("한컴돋움", 0, 12))); // NOI18N
        jPanel4.setName(""); // NOI18N

        jLabel3.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel3.setText("시작 번호");
        jLabel3.setToolTipText("대상자 시작번호 1은 항상 시험 시작 시각과 연동됩니다."); // NOI18N

        groupNumComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5" }));
        groupNumComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupNumComboBoxActionPerformed(evt);
            }
        });
        groupNumComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                groupNumComboBoxPropertyChange(evt);
            }
        });

        startNumTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        startNumTF.setText("1");
        startNumTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startNumTFActionPerformed(evt);
            }
        });

        jLabel21.setText("번");
        jLabel21.setToolTipText("");

        jLabel1.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel1.setText("끝번호");
        jLabel1.setToolTipText("최대50명으로 제한합니다.");

        groupSettingBT.setForeground(new java.awt.Color(204, 204, 204));
        groupSettingBT.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/redarrow20.png"))); // NOI18N
        groupSettingBT.setToolTipText(""); // NOI18N
        groupSettingBT.setBorder(null);
        groupSettingBT.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        groupSettingBT.setMaximumSize(new java.awt.Dimension(25, 25));
        groupSettingBT.setMinimumSize(new java.awt.Dimension(25, 25));
        groupSettingBT.setName(""); // NOI18N
        groupSettingBT.setPreferredSize(new java.awt.Dimension(25, 25));
        groupSettingBT.setRequestFocusEnabled(false);
        groupSettingBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupSettingBTActionPerformed(evt);
            }
        });

        endNumTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        endNumTF.setText("20");

        jLabel17.setText("명");
        jLabel17.setToolTipText(""); // NOI18N

        jLabel4.setText("그룹수");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(startNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21))
                    .addComponent(groupNumComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(endNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel17))
                    .addComponent(groupSettingBT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(112, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(startNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(jLabel1)
                    .addComponent(endNumTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(groupNumComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(groupSettingBT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel1, jLabel17, jLabel3});

        leftsettingPanel.add(Box.createRigidArea(new Dimension(0,10)));

        leftsettingPanel.add(jPanel4);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "시험시간 설정", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("한컴돋움", 0, 12))); // NOI18N
        jPanel6.setMaximumSize(new java.awt.Dimension(400, 294));

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
                { new Double(0.0), ":",  new Integer(0), "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"},
                {null, ":", null, "All"}
            },
            new String [] {
                "시", ":", "분", "그룹"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true, true
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
        periodSettingTable.getTableHeader().setReorderingAllowed(false);
        timeSetTablePanel.setViewportView(periodSettingTable);
        if (periodSettingTable.getColumnModel().getColumnCount() > 0) {
            periodSettingTable.getColumnModel().getColumn(0).setMinWidth(20);
            periodSettingTable.getColumnModel().getColumn(0).setPreferredWidth(45);
            periodSettingTable.getColumnModel().getColumn(0).setMaxWidth(60);
            periodSettingTable.getColumnModel().getColumn(1).setMinWidth(10);
            periodSettingTable.getColumnModel().getColumn(1).setPreferredWidth(15);
            periodSettingTable.getColumnModel().getColumn(1).setMaxWidth(15);
            periodSettingTable.getColumnModel().getColumn(2).setMinWidth(30);
            periodSettingTable.getColumnModel().getColumn(2).setPreferredWidth(45);
            periodSettingTable.getColumnModel().getColumn(2).setMaxWidth(60);
            periodSettingTable.getColumnModel().getColumn(3).setMinWidth(20);
            periodSettingTable.getColumnModel().getColumn(3).setPreferredWidth(55);
            periodSettingTable.getColumnModel().getColumn(3).setMaxWidth(60);
        }
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        periodSettingTable.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );

        JTableHeader header = periodSettingTable.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) periodSettingTable.getTableHeader().getDefaultRenderer();
        header.setDefaultRenderer(headerRenderer);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        jLabel16.setText("분");

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel2.setText("대상자간 간격");
        jLabel2.setToolTipText("동일 샘플링 시험의 다음 대상자를 부를때까지의 시간간격입니다.");

        gapTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gapTF.setText("1");

        jLabel27.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel27.setText("샘플링 주기 시간/그룹 설정");

        displayDateLabel.setText(" ");
        //

        cButton.setBorder(null);
        cButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/calendar20.png"))); // NOI18N
        cButton.setToolTipText(""); // NOI18N
        cButton.setAlignmentY(0.0F);
        cButton.setAutoscrolls(true);
        cButton.setFont(new java.awt.Font("굴림", 0, 8)); // NOI18N
        cButton.setHideActionText(true);
        cButton.setIconTextGap(0);
        cButton.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                cButtonPropertyChange(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        jLabel8.setText("시험 시작 ");
        jLabel8.setToolTipText(""); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(gapTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16))
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(cButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(displayDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hourCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addComponent(timeSetTablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {hourCB, minCB});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(minCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hourCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(displayDateLabel)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel27)
                .addGap(10, 10, 10)
                .addComponent(timeSetTablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel16)
                    .addComponent(jLabel2)
                    .addComponent(gapTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        leftsettingPanel.add(Box.createRigidArea(new Dimension(0,5)));

        leftsettingPanel.add(jPanel6);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "알림 설정", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("한컴돋움", 0, 12))); // NOI18N
        jPanel7.setFont(new java.awt.Font("한컴돋움", 0, 12)); // NOI18N

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

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(102, 102, 102)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(enableCallingCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addGap(30, 30, 30)
                        .addComponent(callingTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(enablePreparingCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel14)
                        .addGap(30, 30, 30)
                        .addComponent(preparingTF, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)))
                .addContainerGap(117, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enablePreparingCB)
                    .addComponent(preparingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(enableCallingCB)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(callingTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {enableCallingCB, jLabel5});

        leftsettingPanel.add(Box.createRigidArea(new Dimension(0,5)));

        leftsettingPanel.add(jPanel7);
        leftsettingPanel.add(Box.createRigidArea(new Dimension(0,10)));

        settingPanel.setLeftComponent(leftsettingPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(settingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );

        settingPanel.setRightComponent(new MyRadioPanel());

        settingContainer.add(jPanel1);

        jPanel2.setMinimumSize(new java.awt.Dimension(0, 65));
        jPanel2.setPreferredSize(new java.awt.Dimension(592, 67));
        jPanel2.setRequestFocusEnabled(false);

        createBT.setBackground(new java.awt.Color(51, 51, 51));
        createBT.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        createBT.setForeground(new java.awt.Color(255, 255, 255));
        createBT.setText("생성");
        createBT.setMargin(new java.awt.Insets(2, 10, 2, 10));
        createBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createBTActionPerformed(evt);
            }
        });

        clearBT.setBackground(new java.awt.Color(51, 51, 51));
        clearBT.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        clearBT.setForeground(new java.awt.Color(255, 255, 255));
        clearBT.setText("지우기");
        clearBT.setEnabled(false);
        clearBT.setMargin(new java.awt.Insets(2, 10, 2, 10));
        clearBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBTActionPerformed(evt);
            }
        });

        stopBT.setBackground(new java.awt.Color(51, 51, 51));
        stopBT.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        stopBT.setForeground(new java.awt.Color(255, 255, 255));
        stopBT.setText("정지");
        stopBT.setEnabled(false);
        stopBT.setMargin(new java.awt.Insets(2, 10, 2, 10));
        stopBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBTActionPerformed(evt);
            }
        });

        runBT.setBackground(new java.awt.Color(51, 51, 51));
        runBT.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        runBT.setForeground(new java.awt.Color(255, 255, 255));
        runBT.setText("시작");
        runBT.setEnabled(false);
        runBT.setMargin(new java.awt.Insets(2, 10, 2, 10));
        runBT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runBTActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(createBT, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(68, 68, 68)
                .addComponent(clearBT)
                .addGap(171, 171, 171)
                .addComponent(runBT)
                .addGap(69, 69, 69)
                .addComponent(stopBT)
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clearBT, createBT, runBT, stopBT});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(createBT, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(clearBT, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(runBT, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(stopBT, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(20, 20, 20))
        );

        stopBT.getAccessibleContext().setAccessibleDescription("");

        settingContainer.add(jPanel2);

        jTabbedPanel.addTab("세팅", settingContainer);

        tablePanel.setLayout(new javax.swing.BoxLayout(tablePanel, javax.swing.BoxLayout.Y_AXIS));

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

        tablePanel.add(timeTableJSPane);

        jPanel5.setMaximumSize(new java.awt.Dimension(32767, 30));
        jPanel5.setPreferredSize(new java.awt.Dimension(119, 30));
        jPanel5.setRequestFocusEnabled(false);

        exportToExcelBTN.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/excel_20.png"))); // NOI18N
        exportToExcelBTN.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToExcelBTNActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(exportToExcelBTN, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 985, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(exportToExcelBTN)
                .addGap(0, 1, Short.MAX_VALUE))
        );

        tablePanel.add(jPanel5);

        jTabbedPanel.addTab("스케쥴", tablePanel);

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

        aboutPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText("Authors:");

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText("Web:");

        jLabel24.setFont(new java.awt.Font("굴림체", 1, 14)); // NOI18N
        jLabel24.setText("김민걸 & 이지형");

        jLabel25.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel25.setText("Tea Factory Team (info@teafactory.co)");

        jLabel26.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel26.setText("http://www.teafactory.co");

        jLabel11.setFont(new java.awt.Font("굴림체", 1, 14)); // NOI18N
        jLabel11.setText("이 프로그램은 전북대학교병원 임상시험 글로벌선도센터의 후원으로 제작되었습니다.");

        jLabel28.setFont(new java.awt.Font("Agency FB", 0, 36)); // NOI18N
        jLabel28.setText("Rooibos  @ Tea Factory");

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/Rooibos_logo_128.jpg"))); // NOI18N

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/teafactory.jpg"))); // NOI18N

        javax.swing.GroupLayout aboutPanelLayout = new javax.swing.GroupLayout(aboutPanel);
        aboutPanel.setLayout(aboutPanelLayout);
        aboutPanelLayout.setHorizontalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(130, 130, 130)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 604, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(129, 129, 129)
                        .addComponent(jLabel18)
                        .addGap(24, 24, 24)
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(131, 131, 131)
                        .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(aboutPanelLayout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addGap(40, 40, 40)
                                .addComponent(jLabel19))
                            .addGroup(aboutPanelLayout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addGap(25, 25, 25)
                                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel26)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(283, Short.MAX_VALUE))
        );
        aboutPanelLayout.setVerticalGroup(
            aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutPanelLayout.createSequentialGroup()
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(125, 125, 125)
                        .addComponent(jLabel28)
                        .addGap(37, 37, 37)
                        .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel22)
                            .addComponent(jLabel25))
                        .addGap(10, 10, 10)
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(aboutPanelLayout.createSequentialGroup()
                        .addGap(69, 69, 69)
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aboutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel23)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(181, 181, 181))
        );

        jTabbedPanel.addTab("루이보스는", aboutPanel);

        footerPanel.setMinimumSize(new java.awt.Dimension(1024, 40));
        footerPanel.setOpaque(false);
        footerPanel.setPreferredSize(new java.awt.Dimension(1024, 40));
        footerPanel.setLayout(new javax.swing.BoxLayout(footerPanel, javax.swing.BoxLayout.LINE_AXIS));

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgPackage/전북대병원로고.png"))); // NOI18N
        jLabel13.setAutoscrolls(true);
        jLabel13.setMaximumSize(new java.awt.Dimension(200, 35));
        jLabel13.setMinimumSize(new java.awt.Dimension(200, 35));
        jLabel13.setName(""); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(200, 35));
        jLabel13.setVerifyInputWhenFocusTarget(false);
        footerPanel.add(jLabel13);

        errorLabel.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        errorLabel.setForeground(new java.awt.Color(166, 0, 48));
        errorLabel.setMaximumSize(new java.awt.Dimension(32985, 35));
        errorLabel.setMinimumSize(new java.awt.Dimension(300, 35));
        errorLabel.setName(""); // NOI18N
        errorLabel.setPreferredSize(new java.awt.Dimension(200, 35));
        errorLabel.setRequestFocusEnabled(false);
        footerPanel.add(errorLabel);

        jLabel15.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Rooibos v2.0.Kor @ Tea Factory 2016");
        jLabel15.setMaximumSize(new java.awt.Dimension(250, 16));
        jLabel15.setMinimumSize(new java.awt.Dimension(250, 16));
        jLabel15.setPreferredSize(new java.awt.Dimension(250, 16));
        footerPanel.add(jLabel15);

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

    private void exportToExcelBTNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToExcelBTNActionPerformed
        try {
            ExcelExporter exp = new ExcelExporter();
            String username = System.getProperty("user.name");
            String fileName = "RoooibosTable.xls";
            String absoluteFile = "C:" + File.separator + File.separator + "Users" + File.separator + username + File.separator + "Desktop" + File.separator + fileName;
            exp.fillData(showScheduleTable, new File(absoluteFile));
            JOptionPane.showMessageDialog(null, fileName + " 파일이 바탕화면에 저장되었습니다.", "Message", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_exportToExcelBTNActionPerformed

    private void enablePreparingCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enablePreparingCBActionPerformed
        enablePreparing = enablePreparingCB.isSelected();

        //    Scheduler.MyTask.setEnablePreparing(enablePreparing);
    }//GEN-LAST:event_enablePreparingCBActionPerformed

    private void minCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_minCBActionPerformed

    private void hourCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hourCBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hourCBActionPerformed

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

    private void groupNumComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupNumComboBoxActionPerformed

 
       groupNumtmp=Integer.parseInt((String) groupNumComboBox.getSelectedItem());     
       
    }//GEN-LAST:event_groupNumComboBoxActionPerformed

    private void groupNumComboBoxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_groupNumComboBoxPropertyChange
        //    if

//        CardLayout card = (CardLayout)mainCardPanel.getLayout();
//
//        card.show(mainCardPanel, "panelOne");
    }//GEN-LAST:event_groupNumComboBoxPropertyChange

    private void calendarButtonPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_calendarButtonPropertyChange
//         if (evt.getNewValue() instanceof Date){
//            setTestDate((Date) evt.getNewValue());
//             
//           }
    }//GEN-LAST:event_calendarButtonPropertyChange

    private void calendarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCalendarButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCalendarButtonActionPerformed

    private void cButtonPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_cButtonPropertyChange
        if (evt.getNewValue() instanceof Date){
            setTestDate((Date) evt.getNewValue());
             
           }
    }//GEN-LAST:event_cButtonPropertyChange

    private void groupSettingBTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupSettingBTActionPerformed
     setErrorMessage("");
        end = Integer.parseInt(endNumTF.getText());
            // pNum=endNum-startNum+1;
       start = Integer.parseInt(startNumTF.getText());

       if(groupNum!=groupNumtmp){ 
        
           groupNum=groupNumtmp;
        //   if(groupNum==1) periodSettingTable.getColumnModel().getColumn(3).setModelIndex(3);//.getCellEditor().stopCellEditing();
          // else periodSettingTable.getColumnModel().getColumn(3).
          
             periodSettingTable.getColumnModel().getColumn(3).setCellEditor(new CheckListEditor(groupNum));

           periodSettingTable.repaint();
       }
       //  settingPanel.setRightComponent(mrp.getRadioPanel());
     
    mrp =new MyRadioPanel(start,end,groupNum);
  //     radioTable=mr.getTable();
    //   radioTable.repaint();
       //rightScrolPanel.repaint();
   //  mrp.makeTable(start, end, groupNum);
  // MyRadioTable mr=new MyRadioTable(start,end,groupNum);
     settingPanel.setRightComponent(mrp.getRadioPanel());
    // rightsettingPanel=mr.getRadioPanel();
     // rightsettingPanel.setVisible(true);//.repaint();
    }//GEN-LAST:event_groupSettingBTActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {

        String dir = System.getProperty("user.dir") + "\\";

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

        /*log file 만들기   */
        //PrintStream console=System.out;//if this is uncomment, it will show the result on console
        File file = new File(dir+"rooibos_log.txt");
//        try {
//            long size = file.length();
//            FileOutputStream fos;
//            if (size > 100000000  )//100M
//            {
//                fos = new FileOutputStream(file);
//            } else {
//                fos = new FileOutputStream(file, true);
//            }
//            PrintStream ps = new PrintStream(fos);
//            System.setOut(ps);
//            //System.setOut(console);//기존방법
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
//        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Rooibos().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aboutPanel;
    private javax.swing.ButtonGroup buttonGroup1;
    private net.sourceforge.jcalendarbutton.JCalendarButton cButton;
    private javax.swing.JTextField callingTF;
    private javax.swing.JButton clearBT;
    private javax.swing.JPanel clockPanel;
    private javax.swing.JPanel containerjPanel;
    private javax.swing.JButton createBT;
    private javax.swing.JLabel dateLabel;
    private static javax.swing.JLabel displayDateLabel;
    private javax.swing.JCheckBox enableCallingCB;
    private javax.swing.JCheckBox enablePreparingCB;
    private javax.swing.JTextField endNumTF;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton exportToExcelBTN;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JTextField gapTF;
    private javax.swing.JComboBox groupNumComboBox;
    private javax.swing.JButton groupSettingBT;
    private javax.swing.JComboBox hourCB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTabbedPane jTabbedPanel;
    private javax.swing.JPanel leftsettingPanel;
    private javax.swing.JComboBox minCB;
    private javax.swing.JTable periodSettingTable;
    private javax.swing.JTextField preparingTF;
    private javax.swing.JPanel rightsettingPanel;
    private javax.swing.JButton runBT;
    private javax.swing.JPanel settingContainer;
    private javax.swing.JSplitPane settingPanel;
    private javax.swing.JTable showScheduleTable;
    private javax.swing.JTextField startNumTF;
    private javax.swing.JButton stopBT;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JScrollPane timeSetTablePanel;
    private javax.swing.JScrollPane timeTableJSPane;
    // End of variables declaration//GEN-END:variables
//public static Date getDate(){
//    return testDate;
//}

    public void setTestDate(Date date) {

        Calendar cal = Calendar.getInstance();
        if (date == null) {
           cal.setTimeInMillis(System.currentTimeMillis());
          // date=cal.getTime();
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

    private void setErrorMessage(String s) {
        errorLabel.setText(s);
    }

    private void startBroadcast() {
        long currentTime = System.currentTimeMillis();

        if ((Long) treemap.lastKey() < currentTime / 1000) {
            System.out.println("the test time you set are passed to call. change start time.");
            setErrorMessage("테스트 시간이 지났습니다. 시작시간을 다시 정해주세요.");
            return;
        }
        Calendar x = Calendar.getInstance();
        x.setTimeInMillis(currentTime);
        long curMillis = currentTime - (currentTime / 1000) * 1000;
        long delayMillis = 1000 - curMillis + 1; // 정시보다 1 miliseconed 를 줘서 오류를 방지한다.

        System.out.println("\t" + x.getTime() + "\tstart btn pushe in rooibos.java   current time mlisdecond:" + curMillis + "\tdelaytime milisecond:  " + delayMillis);

        java.util.Timer runTimer = new java.util.Timer();
        runTimer.scheduleAtFixedRate(new MyTask(), delayMillis, 1000);
    }
    public int count = 7;

    private class MyTask extends TimerTask {

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
            long preparingtimeSec = currentTimeSec + preparingSec;
            long countingtimeSec = currentTimeSec + 6;
            long callingtimeSec = currentTimeSec + (callingMin * 60 - callingOffset);

            if ((Long) treemap.lastKey() < currentTimeSec) {
                System.out.println("isDoneATask");
                this.cancel();
                return;
            }
            if (isStopped) {
                System.out.println("doCancel");
                this.cancel();
                return;
            }
            if (count < 6) {

                try {
                    pr.playFile(new File(dir + cnt[count++] + "S.wav"));
                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
                }
               // return;

            } else if (treemap.containsKey(countingtimeSec)) {
                count = 0;
            } else if (enableCalling && treemap.containsKey(callingtimeSec)) {//add 1 to round up because this should be check for right on minute like  xx:xx:00.
                List<Integer> list = (List<Integer>) treemap.get(callingtimeSec);

                pr.playCalling(list);
                //   return;
            } else if (enablePreparing && treemap.containsKey(preparingtimeSec)) {//add 1 to round up because this should be check for right on minute like  xx:xx:00.
                List<Integer> list = (List<Integer>) treemap.get(preparingtimeSec);
                pr.playPreparing(list);
                // return;
            }

//            if (treemap.containsKey(countingtimeSec)) {//add 1 to round up because this should be check for right on minute like  xx:xx:00.
//                pr.resetCount();
//                pr.playCounting();               
//            }
        }

    }
    final String dir = System.getProperty("user.dir") + "\\sounds\\";
    String[] cnt = {"55", "56", "57", "58", "59", "00"};

}
