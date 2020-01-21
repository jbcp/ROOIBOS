
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * 해당 음성파일을 실행
 * @author Ji-hyoung Lee
 */
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Timer;
import java.util.TimerTask;

public class Player {

    final String dir = System.getProperty("user.dir") + "\\sounds\\";
    private static final int BUFFER_SIZE = 1024 * 1024;
    private boolean isInterrupted;
    private static int count = 0;
    AudioInputStream audioStream = null;
    SourceDataLine sourceLine = null;

    public void playFile(File file) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());              
        System.out.println(cal.getTime() + " \t " + file.getName() + "\t" + System.currentTimeMillis());

        try {
            audioStream = AudioSystem.getAudioInputStream(file);
            final AudioFormat audioFormat = audioStream.getFormat();
            final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
            sourceLine.start();

            int bytesRead = 0;
            final byte[] buffer = new byte[BUFFER_SIZE];
            while (!isInterrupted && (bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1) {
                // if(isInterrupted) return;
                if (bytesRead >= 0) {
                    sourceLine.write(buffer, 0, bytesRead);
                }
            }
        } finally {
            if (audioStream != null) {
                audioStream.close();
            }

            if (sourceLine != null) {
                sourceLine.drain();
                sourceLine.close();
            }
        }
    }

    public void cancelPlay() throws IOException {
        try {
            sourceLine.stop();
            sourceLine.flush();
            audioStream.close();
        } catch (IOException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (audioStream != null) {
                audioStream.close();
            }

            if (sourceLine != null) {
                sourceLine.drain();
                sourceLine.close();
            }

        }
    }

    void playCalling(List<Integer> list) {

        System.out.println("calling-----------------   ");
        File gap = new File(dir + "gap01.wav");

        Thread thread = new Thread() {
            @Override
            public void run() {

                try {

                    for (int s : list) {
                        playFile(new File(dir + s + ".wav"));
                        playFile(gap);                             
                    }
                    playFile(new File(dir + "comeUp.wav"));

                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        thread.start();

    }

    void playPreparing(List<Integer> list) {
        System.out.println("preparing-----------------   ");
        File gap = new File(dir + "gap01.wav");
        Thread thread = new Thread() {
            @Override
            public void run() {               
                try {
                    for (int i : list) {
                        playFile(new File(dir + i + "_1.wav"));
                        playFile(gap);
                    }
                    playFile(new File(dir + "preparing.wav"));

                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        thread.start();

    }

    public void resetCount() {
        this.count = 0;
        /*A-block : 프로그램 끝날때까지 컴퓨터가 sleep mode 로 빠지는 것을 방지*/
        try {
            Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
            Robot rob = new Robot();
            rob.mouseMove(mouseLoc.x, mouseLoc.y);
        } catch (AWTException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        /* A-block end */
    }

    void playCounting() {

        String[] cnt = {"55", "56", "57", "58", "59", "00"};

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (count >= 6) {
                    //   System.out.println("cancle count==5");
                    this.cancel();
                    //    this.purge();
                    return;
                }                     

                try {
                    playFile(new File(dir + cnt[count++] + "S.wav"));
                    // test.doStuff();

                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(Player.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Player.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(Player.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

            }

        }, 0, 1000);

    }
}
