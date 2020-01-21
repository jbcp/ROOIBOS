
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.swing.JRadioButton;

/**
 * showScheduleTable에 보여줄 스케쥴 생성 1) Treemap 에 이 스케쥴을 저장 2) Data와 column title 을
 * return 한다.
 *
 * @author jhlee
 */
public class MyScheduleTableModelHelper {

    int index_standard = 3;
    private boolean[][] state;
    private Date testDate;
    private Object[][] data;

    public String[] title = {"대상자", "그룹", "활성화", "1st", "2nd", "3rd", "4th", "5th", "6th",
        "7th", "8th", "9th", "10th", "11th", "12th", "13th", "14th", "15th", "16th",
        "17th", "18th", "19th", "20th", "21th", "22th", "23th", "24th", "25th", "26th", "27th", "28th", "29th", "30th"};
    // private String[] selectedArr = new String[]{"A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A"};
    int start;
    int end;
    //inal int indexColNum = 3;//대상자, 그룹, 활성화
    private String[] columnNames;
    private int callOffset = 5;//sec

    public MyScheduleTableModelHelper(int rowNum, int colNum) {
        columnNames = Arrays.copyOfRange(title, 0, colNum);
        // treemap = new TreeMap<Long, List<Integer>>();
        if (rowNum == 0) {
            this.data = null;
            return;
        }

        this.data = new Object[rowNum][columnNames.length];
        this.state = new boolean[rowNum][columnNames.length];
        // Arrays.fill(isFill, false);

        for (int j = 0; j < columnNames.length; j++) {
            if (j == 0) {
                for (int i = 0; i < rowNum; i++) {
                    data[i][0] = (i + 1);
                }
            } else {
                for (int i = 0; i < rowNum; i++) {
                    data[i][j] = null;
                }
            }
        }
    }

    public void setTitle(String[] header) {
        title = header;
    }
    /*
     public long getSecToMillis(int sec) {

     long currentTime = System.currentTimeMillis();
     Calendar timeNow = Calendar.getInstance();

     timeNow.setTimeInMillis(currentTime);

     timeNow.set(Calendar.HOUR_OF_DAY, 0);
     timeNow.set(Calendar.MINUTE, 0);
     timeNow.set(Calendar.SECOND, 0);
     timeNow.set(Calendar.MILLISECOND, 0);      Date d = timeNow.getTime();
       
     return d.getTime() + sec * 1000;
     }*/
//    public long getSecToTodaySec(int sec) {
//
//        long currentTime = System.currentTimeMillis();
//        Calendar timeNow = Calendar.getInstance();
//
//        timeNow.setTimeInMillis(currentTime);
//
//        timeNow.set(Calendar.HOUR_OF_DAY, 0);
//        timeNow.set(Calendar.MINUTE, 0);
//        timeNow.set(Calendar.SECOND, 0);
//        timeNow.set(Calendar.MILLISECOND, 0);
//        Date d = timeNow.getTime();
//       
//        return testDate.getTime()/1000 + sec ;//* 1000;
//    }

    public boolean[][] getState() {

        return state;
    }

    public List<Integer> getPeriodTimeList() {
        return periodTiemList;
    }
    final int isCOUNT = 0;
    final int isPREPARE = 1;
    final int isCALL = 2;
    int prepare;

    private List<Integer> periodTiemList;
    // private List<Integer> periodCallList;
    private List<String[]> periodGroupCallList;

    private void makeModel(int[] setting, Map<Integer, String[]> periodGroupMap, Date testDate, String[] groupSelectionArr) {
  SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.EEE ");
        String formattedString = formatter.format(testDate);        
System.out.println("ModelHelper makeModel"+formattedString);
        this.state = new boolean[end][columnNames.length];//default=false;
        prepare = setting[Rooibos.PREPARE];
//      setTitle(setting[Rooibos.COL_DATA_START]);
        // String[] StandardStr = periodGroupMap.get(setting[Rooibos.STANDARD_TODAY_MIN]);
        //   periodGroupMap.remove(setting[Rooibos.STANDARD_TODAY_MIN]);

        periodTiemList = new ArrayList<Integer>(periodGroupMap.keySet());
        periodGroupCallList = new ArrayList<String[]>(periodGroupMap.values());
        //   periodTiemList.add(0, setting[Rooibos.STANDARD_TODAY_MIN]);
        // periodGroupCallList.add(0, StandardStr);

        this.testDate = testDate;
        // treemap = new TreeMap<Long, List<Integer>>();

        int rowNum = end;

        this.data = new Object[rowNum][columnNames.length];

        for (int j = 0; j < columnNames.length; j++) {

            if (j == 0) {//대상자번호칼럼 put subjectNumber for colum 0; 
                for (int i = 0; i < rowNum; i++) {
                    data[i][0] = (i + 1);
                    if (i >= start - 1) {
                        this.state[i][j] = true;
                    }
                    //  System.out.println("rowNum="+rowNum+"startNum="+startNum+"["+i+"]["+j+"]=    "+data[i][j]);
                }
            } else if (j == 1) {//그룹표시칼럼,선택한 그룹 표시
                //  String group = periodGroupList.get(j - 1);
                for (int i = 0; i < rowNum; i++) {
                    if (i < end && i >= start - 1) {
                          if(setting[Rooibos.GROUP]==1) data[i][1] = "All";    
                          else data[i][1] = groupSelectionArr[i + 1];
                        this.state[i][j] = true;
                    }
                }
            } else if (j == 2) {//활성화
                for (int i = 0; i < rowNum; i++) {

                    if (i >= start - 1) {
                        data[i][j] = new Boolean(true);
                        this.state[i][j] = true;
                    } else {
                        data[i][j] = new Boolean(false);
                        this.state[i][j] = false;
                    }
                    //  System.out.println("rowNum="+rowNum+"startNum="+startNum+"["+i+"]["+j+"]=    "+data[i][j]);
                }

            } else if (j < (periodTiemList.size() + Rooibos.COL_DATA_START)) { //데이타 칼럼
                int dataCol = j - Rooibos.COL_DATA_START;///대상자, 그룹 칼럼, 활성화칼럼 제와을 제외. j=3부터 시작하는 col은 왼쪽 Schedules 표의 row 값과 동일

                for (int i = 0; i < rowNum; i++) {

                    int setMin = periodTiemList.get(dataCol) + setting[Rooibos.GAP] * i; //min
                    if (setMin == setting[Rooibos.STANDARD_MIN_FROM_MIDNIGHT]) {
                        setStandardColIndex(j);
                    }

                    long testTimeMilis = testDate.getTime();
        //   List<Integer> pList;//=new ArrayList<Integer>();

                 //   Calendar time = Calendar.getInstance();
                    //  time.setTimeInMillis(testTimeSec * 1000);
                    data[i][j] = new Date(testTimeMilis + setMin * 60 * 1000);
                    //System.out.println(setMin + "row " + i + "  col" + j + "   periodTiemList.get(dataCol)" + periodTiemList.get(dataCol) + "  setting[Rooibos.GAP] * i =" + setting[Rooibos.GAP] * i+"==?"+testTimeMilis+setMin*60*1000);

                    if (i >= start - 1) {
                        //start check group
                        String groupsOfSample = periodGroupCallList.get(dataCol)[0];
                        if (groupsOfSample.equals("All")) {
                            //    System.out.println("All :   " + (i + 1) + "-->" + group);
                            //addToHashTable(setMin * 60, i + 1);//subject Num=i+1 //<--Data to call
                            this.state[i][j] = true;
                        } else if (groupsOfSample.contains(groupSelectionArr[1 + i])) {//selectedArr is groupSelection of each subject. 
                            //end
                            this.state[i][j] = true;
                            //   addToHashTable(setMin * 60, i + 1);//subject Num=i+1 //<--Data to call
                            //  System.out.println("add :   " + (i + 1) + "-->" + group);
                        } else {
                            //    System.out.println("not add :   " + (i + 1) + "-->" + group);
                        }

                    }
                }
            }
        }

//   checking tree map datas
//        Calendar time = Calendar.getInstance();
//        treemap.entrySet().stream().forEach(
//                (entry) -> {
//                    Long key = entry.getKey();
//                    List<Integer> value = entry.getValue();
//                    time.setTimeInMillis(key * 1000);
//
//                    System.out.println("MyTableModelHelper\t" + time.getTime() + " => " + value);
//                });
    }

    public MyScheduleTableModelHelper(int[] setting, TreeMap<Integer, String[]> periodGroupMap, Date testDate, String[] groupSelectionArr) {
        this.start = setting[Rooibos.START];
        this.end = setting[Rooibos.END];
        int colNum = periodGroupMap.size() + Rooibos.COL_DATA_START;
        if (colNum < 10) {
            colNum = 10;
        }
        this.columnNames = Arrays.copyOfRange(title, 0, colNum);
        makeModel(setting, periodGroupMap, testDate, groupSelectionArr);
    }

//    public MyScheduleTableModelHelper(int start, int end, int callGapTime, TreeMap<Integer, String> periodGroupMap, Date testDate) {
//this.start=start;
//this.end=end;
//        makeModel(callGapTime, periodGroupMap, testDate, selectedArr);
//
//    }
//    public MyTableModelHelper(int startNum, int pNum, int startTime, int callGapTime, List<Integer> periodTimeList, Date testDate) {
//        this.testDate=testDate;
//        treemap = new TreeMap<Long, List<Integer>>();
//        int colNum = periodTimeList.size() + 2;/*대상자번호칼럼과 시험시작시간 칼럼이 추가
//         // 시험시간 후 샘플링타임리스트이므로 기준점 시험시작시간칼럼이 필요*/
//    
//        int rowNum = pNum + startNum - 1;
//
//        this.data = new String[rowNum][columnNames.length];
//// System.out.println("MyTableModelHelper------startTime" +startTime + "testDate" + testDate.toString());
//        for (int j = 0; j < columnNames.length; j++) {
//            if (j == 0) {
//                for (int i = 0; i < rowNum; i++) {
//                    data[i][0] = "" + (i + 1);
// //  System.out.println("rowNum="+rowNum+"startNum="+startNum+"["+i+"]["+j+"]=    "+data[i][j]);
//                }
//            } else if (j == 1) {
//                for (int i = 0; i < rowNum; i++) {
//                    int setMin = startTime + callGapTime * i;
//                    data[i][j] = minToTime(setMin);
//                    int tmpSec = setMin * 60;
//                    if (i >= startNum - 1) {
//                        addToHashTable(tmpSec, i + 1); //key should be sec.
//                    }   
//  // System.out.println("["+i+"]["+j+"]=    "+data[i][j]);
//                }
//            } else if (j >= colNum) {
//                for (int i = 0; i < rowNum; i++) {
//                    data[i][j] = "";
//                }
//            } else {
//                for (int i = 0; i < rowNum; i++) {
//                    int setMin = startTime + periodTimeList.get(j - 2) + callGapTime * i; //min
//                    data[i][j] = minToTime(setMin);
//                    if (i >= startNum - 1) {
//                        addToHashTable(setMin * 60, i + 1);
//                    }                    
//                }
//            }
//        }
//
////   checking tree map datas
//        Calendar time = Calendar.getInstance();
//        treemap.entrySet().stream().forEach(
//                (entry) -> {
//                    Long key = entry.getKey();
//                    List<Integer> value = entry.getValue();
//                    time.setTimeInMillis(key*1000);                
//                   
//                  System.out.println("MyTableModelHelper\t" + time.getTime() + " => " + value);
//                });
//
//    }
//    public Object[][] getNewData(int rowNum) {
//
//        this.data = new Object[rowNum][columnNames.length];
//        for (int i = 0; i < rowNum; i++) {
//            data[i][0] = (i + 1);
//        }
//        return this.data;
//    }
//
//    public int getColumnCount() {
//        return columnNames.length;
//    }
//
//    public int getRowCount() {
//
//        return data.length;
//    }
//    public void clearData() {
//        int colNum = getColumnCount();
//        int rowNum = getRowCount();
//        //   System.out.println("rowNum" + rowNum);
//        for (int j = 0; j < colNum; j++) {
//            if (j == 0) {
//                for (int i = 0; i < rowNum; i++) {
//                    data[i][0] =  (i + 1);
//
//                    // System.out.println("["+i+"]["+j+"]=    "+data[i][j]);
//                }
//            } else {
//                for (int i = 0; i < rowNum; i++) {
//                    data[i][j] = null;
//                }
//            }
//        }
//    }
//    private String minToTime(int t) {//08:00  15:00
//        return String.format(FORMAT,
//                TimeUnit.MINUTES.toHours(t) - TimeUnit.DAYS.toHours(TimeUnit.MINUTES.toDays(t)),
//                TimeUnit.MINUTES.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(t)));
//    }
    public TreeMap getDB() {
        //   checking tree map datas
        Calendar time = Calendar.getInstance();

        treemap.entrySet().stream().forEach(
                (entry) -> {
                    Long key = entry.getKey();
                    List<ASchedule> value = (List<ASchedule>) entry.getValue();

                    time.setTimeInMillis(key*1000);
                  //  for (int i = 0; i < value.size(); i++) {
                        //  if (value.get(i).getSubject() < 56) {
                        System.out.println("MyTableModelHelper\t" + time.getTime() + " => " + value.toString());
                        //  }
                   // }
                });
        return this.treemap;
    }
    private TreeMap<Long, List<ASchedule>> treemap;
    final static int TYPE_COUNT = 0;
    final static int TYPE_PREPARE = 1;
    final static int TYPE_CALL = 2;

    public void setDB(Object[][] data, boolean[][] state) {

        //   System.out.println("============================");
        treemap = new TreeMap<Long, List<ASchedule>>();
        int numRows = end;
        int numCols = periodTiemList.size() + Rooibos.COL_DATA_START;
//   System.out.println("==periodGroupCallList.size()" + periodGroupCallList.size());

        //  int aDaySec = 24 * 60 * 60;
        for (int j = Rooibos.COL_DATA_START; j < numCols; j++) {
            for (int i = start - 1; i < numRows; i++) {
                //   int setMin = periodTiemList.get(j - Rooibos.COL_DATA_START) + setting[Rooibos.GAP] * i; //min
                //  data[i][j] = minToTime(setMin);

                if (state[i][j]) {
     //   System.out.println("    row " + i + "  col:" + j + "   " + data[i][j]);
                    //
                    //  System.out.println(time.getTime() + "++++++++++++++" + pNum);

                    Date date = (Date) data[i][j];
  SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.EEE ");
        String formattedString = formatter.format(date);        
System.out.println("ModelHelper setDB  ["+i+"]["+j+"+]" + formattedString);


                    long keySec=date.getTime()/1000;
                    addToHashTable(keySec - 5, 55, TYPE_COUNT);//subject Num=i+1 //<--Data to call

                    addToHashTable(keySec - 4, 56, TYPE_COUNT);//subject Num=i+1 //<--Data to call
                    addToHashTable(keySec - 3, 57, TYPE_COUNT);//subject Num=i+1 //<--Data to call
                    addToHashTable(keySec - 2, 58, TYPE_COUNT);//subject Num=i+1 //<--Data to call
                    addToHashTable(keySec - 1, 59, TYPE_COUNT);//subject Num=i+1 //<--Data to call
                    addToHashTable(keySec, 00, TYPE_COUNT);//subject Num=i+1 //<--Data to call

                    addToHashTable(keySec - prepare , i + 1, TYPE_PREPARE);//subject Num=i+1 //<--Data to call
                    int ts = Integer.parseInt(periodGroupCallList.get(j - Rooibos.COL_DATA_START)[1].trim());
                   

                    if (ts > 0) {
      // System.out.print(ts +"calling==================");
                        int call = ts * 60 - callOffset;
                        addToHashTable(keySec - call, i + 1, TYPE_CALL);//subject Num=i+1 //<--Data to call
                    }
                }
                //  System.out.print("  " + data[i][j]);
            }
            //  System.out.println();
        }
        // System.out.println("--------------------------");
        //   checking tree map datas
//        Calendar time = Calendar.getInstance();
//        
//        treemap.entrySet().stream().forEach(
//                (entry) -> {
//                    Long key = entry.getKey();
//                    List<ASchedule> value = (List<ASchedule>) entry.getValue();
//
//                    time.setTimeInMillis(key);
//                    for (int i = 0; i < value.size(); i++) {
//                      //  if (value.get(i).getSubject() < 56) {
//          System.out.println("MyTableModelHelper\t" + time.getTime() + " => " + value.get(i).getType() + "\t" + value.get(i).getSubject());
//                      //  }
//                    }
//                });
    }

    private void addToHashTable(long key, int pNum, int type) {
        /*시간대별로 호출 정렬 (time,피험자번호)  
         * 시작 시간이 08:00 이라면  480(분)이 저장된다.
         * [480,{1,3}] 8시에 1번과 3번 시험.
         */
        //  long timeMillis = getSecToMillis(sec);
        List<ASchedule> pList;
       
        //   List<Integer> pList;//=new ArrayList<Integer>();

        Calendar time = Calendar.getInstance();
        //time.setTimeInMillis(testTimeSec);
        //time.setTime(date);

  
        if (treemap.containsKey(key)) {
            pList = treemap.get(key);

            if (type == 0) {
                return;
            }
            for (int i = 0; i < pList.size(); i++) {

                if (pList.get(i).getType() > type) {
                    pList.add(i, new ASchedule(type, pNum));
                     //   System.out.println(pList.get(i).getType()+ "+++++++pList.get(i).getType() > type ++++++type+"+ type+"\t"+time.getTime()+"\t"+pList.toString()+  "add pNum "+pNum);

                    break;
                } else if (pList.get(i).getType() == type) {
                    

                    if (pList.get(i).getSubject() > pNum) {
                   //     System.out.println(pList.get(i).getType()+ "+++++++pList.get(i).getType()== type ++++++type+"+ type+"\t"+time.getTime()+"\t"+pList.toString()+  "add pNum "+pNum+"\tpList.get(i).getSubject() > pNum"+pList.get(i).getSubject() );

                        pList.add(i, new ASchedule(type, pNum));
                        break;
                    } else if (pList.get(i).getSubject() == pNum) {
                    //      System.out.println(pList.get(i).getType()+ "+++++++pList.get(i).getType()== type ++++++type+"+ type+"\t"+time.getTime()+"\t"+pList.toString()+  "add pNum "+pNum+"\tpList.get(i).getSubject()== pNum"+pList.get(i).getSubject() );

                        break;
                    }
                    else if(i==pList.size()-1){
                     
                         pList.add(new ASchedule(type, pNum));
                   
                              //System.out.println(pList.get(i).getType()+ "+++++++pList.get(i).getType()== type ++++++type+"+ type+"\t"+time.getTime()+"\t"+pList.toString()+  "add pNum "+pNum+"\tpList.get(i).getSubject()< pNum"+pList.get(i).getSubject() );
                    break;
                    }     

                }
                else{
                        System.out.println(pList.get(i).getType()+ "+++++++pList.get(i).getType()<type ++++++type+"+ type+"\t"+time.getTime()+"\t"+pList.toString()+  "add pNum "+pNum);

                }
            }
        } else {
            pList = new ArrayList<ASchedule>();
            ASchedule aDB = new ASchedule(type, pNum);
            pList.add(aDB);
   //if(type!=0) System.out.println(time.getTime()+ "++++++NEW  +" + type+"add pNum "+pNum);

        }
        treemap.put(key, pList);//.put(time, pList); 
       // if(type!=0) System.out.println( "ADD ==============type+" + type+"add pNum "+pNum+ "\t pList "+ pList.size() +" \t"+ pList.toString());

//        Calendar ccc = Calendar.getInstance();
//        ccc.setTimeInMillis(testTimeSec);
        //System.out.println("adding key=      " +testTimeSec+ "\t " + ccc.getTime());
    }

//    public TreeMap getTreeMap() {
//        return this.treemap;
//    }
    public String[] getColTitleArr() {
        return this.columnNames;
    }

    public Object[][] getData() {
        return this.data;

    }
    private int standardColIndex = 3;

    public void setStandardColIndex(int j) {
        standardColIndex = j;
        //   setTitle(j);
    }

    public int getStandardColIndex() {
        return standardColIndex;
    }

    class ASchedule {

        private int type;//0: counting, 1: call 2: prepare
        private int subject;//in case of 0: 55,56,57,58,59,00 etc. subject number

//        public ADB(int type, List<Object> list) {
//            this.type =type;
//            this.list = list;
//        }
        private ASchedule(int type, int subject) {
            this.type = type;
            this.subject = subject;
        }

        public int getType() {
            return this.type;
        }

        public int getSubject() {
            return this.subject;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setList(int sub) {
            this.subject = sub;
        }

        public String toString() {
            String t_tmp = "COUNT";
            if (this.type == 1) {
                t_tmp = "PREPARE";
            } else if (this.type == 2) {
                t_tmp = "CALL";
            }

            return t_tmp + "\tsubject= " + this.subject;
        }

        public boolean equals(int type, int subject) {
            if ((this.type == type) && (this.subject == subject)) {
                return true;
            }
            return false;
        }

        public boolean equals(ASchedule aDB2) {
            if ((this.type == aDB2.getType()) && (this.subject == aDB2.getSubject())) {
                return true;
            }
            return false;
        }
//        public void addListItem(int obj) {
//            if (this.list.size() > 0) {
//                for (int i = 0; i < list.size(); i++) {
//                    if (((Integer) list.get(i)) > obj) {
//                        list.add(i, obj);
//                        return;
//                    }
//                }
//            }
//            this.list.add(obj);
//        }
    }
}
