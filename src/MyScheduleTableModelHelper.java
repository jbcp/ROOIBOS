
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * showScheduleTable에 보여줄 스케쥴 생성 1) Treemap 에 이 스케쥴을 저장 2) Data와 column title 을
 * return 한다.
 *
 * @author jhlee
 */
public class MyScheduleTableModelHelper {

    private boolean[][] status;
    private Date testDate;
    private String[][] data;
    private String FORMAT = "%02d:%02d";
    private TreeMap<Long, List<Integer>> treemap;
    private String[] columnNames = {"대상자", "그룹", "1st", "2nd", "3rd", "4th", "5th", "6th",
        "7th", "8th", "9th", "10th", "11th", "12th", "13th", "14th", "15th", "16th",
        "17th", "18th", "19th", "20th", "21th", "22th", "23th", "24th", "25th", "26th", "27th", "28th", "29th", "30th"};
   private String[] selectedArr = new String[]{"A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A", "A"};
    int start;
    public MyScheduleTableModelHelper(int rowNum) {
        treemap = new TreeMap<Long, List<Integer>>();
        if (rowNum == 0) {
            this.data = null;
            return;
        }
        this.data = new String[rowNum][columnNames.length];
        this.status = new boolean[rowNum][columnNames.length];
        // Arrays.fill(isFill, false);

        for (int j = 0; j < columnNames.length; j++) {
            if (j == 0) {
                for (int i = 0; i < rowNum; i++) {
                    data[i][0] = "" + (i + 1);
                }
            } else {
                for (int i = 0; i < rowNum; i++) {
                    data[i][j] = "";
                }
            }
        }
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

    public boolean[][] getStatus() {
       
        return status;
    }

    private void makeModel(int callGapTime, TreeMap<Integer, String> periodGroupMap, Date testDate, String[] groupSelectionArr) {
        this.selectedArr = new String[end + 1];//groupSelectionArr;//it contains selected group for each subjects ( which is index)
//      for (int i=0;i<groupSelectionArr.length;i++){
//          System.out.println("======="+i+"-->"+groupSelectionArr[i]);
//      }
        this.status = new boolean[end][columnNames.length];
        
        
        
        for (int i = start; i <= end; i++) {
            // if(i<start) this.selectedArr[i]=null;
            // else {
            this.selectedArr[i] = groupSelectionArr[i - start];
//  System.out.println("in mytableModelhelper====:" + i + "\t" + this.selectedArr[i]);
            //  }
        }

        List<Integer> periodTimeList = new ArrayList<Integer>(periodGroupMap.keySet());
        List<String> periodGroupList = new ArrayList<String>(periodGroupMap.values());

     System.out.println("MyTableModelHelper  key----" + periodTimeList.toString());
    System.out.println("MyTableModelHelper value===" + periodGroupList.toString());
    System.out.println(periodTimeList.size()==periodGroupList.size());
        this.testDate = testDate;
        treemap = new TreeMap<Long, List<Integer>>();
        // int colNum = periodTimeList.size() + 1;/*대상자번호칼럼 추가*/

        int rowNum = end;

        this.data = new String[rowNum][columnNames.length];
// System.out.println("MyTableModelHelper------startTime" +startTime + "testDate" + testDate.toString());
        for (int j = 0; j < columnNames.length; j++) {

            if (j == 0) {//대상자번호칼럼 put subjectNumber for colum 0; 
                for (int i = 0; i < rowNum; i++) {
                    data[i][0] = "" + (i + 1);
                     if (i >= start - 1) this.status[i][0] = true;
                    //  System.out.println("rowNum="+rowNum+"startNum="+startNum+"["+i+"]["+j+"]=    "+data[i][j]);
                }
                //}            else if (j == 1) {//first sampling time
//                for (int i = 0; i < rowNum; i++) {
//                    int setMin = startTime + callGapTime * i;
//                    data[i][j] = minToTime(setMin);
//                    int tmpSec = setMin * 60;
//                    if (i >= startNum - 1) {
//                        addToHashTable(tmpSec, i + 1); //key should be sec.
//                    }   
//  // System.out.println("["+i+"]["+j+"]=    "+data[i][j]);
//                }
            } else if (j == 1) {//그룹표시칼럼,선택한 그룹 표시
              //  String group = periodGroupList.get(j - 1);
                for (int i = 0; i < rowNum; i++) {
                    data[i][1] = this.selectedArr[i + 1];
                    this.status[i][1] = true;
                }
//            } 
//            else if (j >= (periodTimeList.size() + 2)) {//30내에서 주기 데이타 업슨 곳--빈 칼럼 처리
//
//                for (int i = 0; i < rowNum; i++) {
//                    data[i][j] = "";
//                }
            } else if (j < (periodTimeList.size() + 2)) { //데이타 칼럼
                int dataCol = j - 2;///대상자, 그룹 칼럼을 제외. j=2 부터 스케쥴 표 시작
                for (int i = 0; i < rowNum; i++) {

                    int setMin = periodTimeList.get(j - 2) + callGapTime * i; //min
                    data[i][j] = minToTime(setMin);
                    if (i >= start - 1) {
                        //start check group
                        String groupsOfSample = periodGroupList.get(dataCol);//대상자, 그룹 칼럼을 제외
                        if (groupsOfSample.equals("All")) {
                        //    System.out.println("All :   " + (i + 1) + "-->" + group);
                            addToHashTable(setMin * 60, i + 1);//subject Num=i+1 //<--Data to call
                            this.status[i][j] = true;
                        } else if (groupsOfSample.contains(this.selectedArr[i + 1])) {//selectedArr is groupSelection of each subject. 
                            //end
                            this.status[i][j] = true;
                            addToHashTable(setMin * 60, i + 1);//subject Num=i+1 //<--Data to call
                          //  System.out.println("add :   " + (i + 1) + "-->" + group);
                        } else {
                        //    System.out.println("not add :   " + (i + 1) + "-->" + group);
                        }

                    }
                }
            }
        }

//   checking tree map datas
        Calendar time = Calendar.getInstance();
        treemap.entrySet().stream().forEach(
                (entry) -> {
                    Long key = entry.getKey();
                    List<Integer> value = entry.getValue();
                    time.setTimeInMillis(key * 1000);

                    System.out.println("MyTableModelHelper\t" + time.getTime() + " => " + value);
                });
    }
int end;
    public MyScheduleTableModelHelper(int start, int end, int callGapTime, TreeMap<Integer, String> periodGroupMap, Date testDate, String[] groupSelectionArr) {
this.start=start;
this.end=end;

        makeModel( callGapTime, periodGroupMap, testDate, groupSelectionArr);
    }

    public MyScheduleTableModelHelper(int start, int end, int callGapTime, TreeMap<Integer, String> periodGroupMap, Date testDate) {
this.start=start;
this.end=end;
        makeModel(callGapTime, periodGroupMap, testDate, selectedArr);

    }

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
    public String[][] getNewData(int rowNum) {

        this.data = new String[rowNum][columnNames.length];
        for (int i = 0; i < rowNum; i++) {
            data[i][0] = "" + (i + 1);
        }
        return this.data;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {

        return data.length;
    }

    public void clearData() {
        int colNum = getColumnCount();
        int rowNum = getRowCount();
        //   System.out.println("rowNum" + rowNum);
        for (int j = 0; j < colNum; j++) {
            if (j == 0) {
                for (int i = 0; i < rowNum; i++) {
                    data[i][0] = "" + (i + 1);
                    // System.out.println("["+i+"]["+j+"]=    "+data[i][j]);
                }
            } else {
                for (int i = 0; i < rowNum; i++) {
                    data[i][j] = null;
                }
            }
        }
    }

    private String minToTime(int t) {//08:00  15:00
        return String.format(FORMAT,
                TimeUnit.MINUTES.toHours(t) - TimeUnit.DAYS.toHours(TimeUnit.MINUTES.toDays(t)),
                TimeUnit.MINUTES.toMinutes(t) - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(t)));
    }

    private void addToHashTable(int sec, int pNum) {
        /*시간대별로 호출 정렬 (time,피험자번호)  
         * 시작 시간이 08:00 이라면  480(분)이 저장된다.
         * [480,{1,3}] 8시에 1번과 3번 시험.
         */
        //  long timeMillis = getSecToMillis(sec);

        long testTimeSec = testDate.getTime() / 1000 + sec;
        List<Integer> pList;//=new ArrayList<Integer>();

        if (treemap.containsKey(testTimeSec)) {
            pList = (List<Integer>) treemap.get(testTimeSec);
            if (pList.get(0) > pNum) {
                pList.add(0, pNum);
                //	System.out.println("pList.get(0)>pNum         "+pList.get(0)+"   "+pNum);

            } else if (pList.get(pList.size() - 1) < pNum) {
                pList.add(pNum);
                //System.out.println("pList.get(pList.size()-1)<pNum       "+pList.get(pList.size()-1)+"   "+pNum);
            } else {
                for (int i = 1; i < pList.size(); i++) {
                    if ((pList.get(i)) > pNum) {
                        pList.add(i, pNum);
                        // System.out.println("pList.get(i))>pNum       "+pList.get(i)+"   "+pNum);
                        break;
                    }
                }

            }

        } else {
            pList = new ArrayList<Integer>();
            pList.add(pNum);
        }
        treemap.put(testTimeSec, pList);//.put(time, pList); 
    }

    public TreeMap getTreeMap() {
        return this.treemap;
    }

    public String[] getColTitleArr() {
        return this.columnNames;
    }

    public String[][] getData() {
        return this.data;
    }

}
