package com.albertarmea.handsfreeactions;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by aarmea on 5/27/13.
 */
public class LogcatReader {
    public static final String TAG = "LogcatReader";

    private String logBuffer = "main";
    private String logTag = "*:v";
    private long messageExpiration = 1000;

    private boolean running = false;
    private OnLogReceiveListener onLogReceiveListener = null;
    private Process logcatProcess = null;
    private Thread readerThread = null;

    public interface OnLogReceiveListener {
        public abstract void onLogReceive(Date time, String message, String fullMessage);
    }

    public LogcatReader () {
    }

    public LogcatReader(String newBuffer, String newTag, long newExpiration) {
        setFilters(newBuffer, newTag, newExpiration);
    }

    public void setFilters(String newBuffer, String newTag, long newExpiration) {
        logBuffer = newBuffer;
        logTag = newTag;
        messageExpiration = newExpiration;
    }

    public void setOnLogReceiveListener(OnLogReceiveListener listener) {
        onLogReceiveListener = listener;
    }

    public boolean start() {
        // Start the logcat process
        String command = null;
        if (logTag.indexOf(' ') < 0) {
            command = String.format("logcat -b %s -v time -s %s", logBuffer, logTag);
        } else {
            // logcat cannot handle tags containing spaces, so use grep instead
            command = String.format("logcat -b %s -v time | grep \"%s\"", logBuffer, logTag);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            logcatProcess = processBuilder.start();
        } catch (IOException e) {
            Log.wtf(TAG, "logcat threw an IOException");
            return false;
        }

        // Read the output from logcatProcess continuously
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                running = true;
                String dateFormatString = "MM-DD hh:mm:ss.SSS";
                SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
                BufferedReader logcatOutput = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()), 1024);
                while (running) {
                    try {
                        // The complete logcat line without any parsing
                        String fullMessage = logcatOutput.readLine();

                        // The date, represented as a Java Date
                        // Parse the date
                        Date time = dateFormat.parse(fullMessage.substring(0, dateFormatString.length()));
                        // Add the current year because logcat does not do it for you
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(time);
                        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                        time = calendar.getTime();

                        // The message itself, excluding time, priority, tag, PID, etc.
                        String message = fullMessage.substring(fullMessage.indexOf("): ")+3);

                        // Send the message
                        if (onLogReceiveListener != null) {
                            // Only send the message if it's less than getMessageExpiration() milliseconds old
                            if ((new Date()).getTime() - time.getTime() < messageExpiration) {
                                onLogReceiveListener.onLogReceive(time, message, fullMessage);
                            }
                        }
                    } catch (ParseException e) {
                        // Ignore malformed logcat lines
                        Log.v(TAG, "Read invalid logcat line");
                    } catch (IOException e) {
                        Log.wtf(TAG, "logcat threw an IOException");
                        break;
                    }
                }
            }
        };
        readerThread = new Thread(runnable);
        readerThread.start();

        return true;
    }

    public void stop() {
        // TODO: implement!
        running = false;
        logcatProcess.destroy();
    }
}
