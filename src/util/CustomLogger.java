package asl.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 * Custom logger class that is used to efficiently
 * log into unique files in a specified directory.
 */
public class CustomLogger {
    private static Logger logger = Logger.getLogger(CustomLogger.class);
    private BufferedWriter writer = null;
    private final String directoryName;

    public CustomLogger(String directoryName, String threadName) {
        File logDir = new File("log");
        if (!logDir.isDirectory()) {
            logDir.mkdir();
        }

        this.directoryName = "log/" + directoryName;
        File dir = new File(directoryName);
        if (!dir.isDirectory()) {
            dir.mkdir();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
        String date = sdf.format(new Date());
        long currentTime = System.currentTimeMillis();
        File file = new File(directoryName + "/" + date + "_" + threadName + "_" + currentTime + ".log");

        try {
            file.createNewFile();
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(String s) {
        try {
            writer.write(s);
        } catch (IOException e) {
            logger.error("Could not write to file.");
            e.printStackTrace();
        }
    }

    public void println(String s) {
        try {
            writer.write(s);
            writer.newLine();
        } catch (IOException e) {
            logger.error("Could not write to file.");
            e.printStackTrace();
        }
    }

    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            logger.error("Could not flush writer.");
            e.printStackTrace();
        }
    }

    public void close() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}