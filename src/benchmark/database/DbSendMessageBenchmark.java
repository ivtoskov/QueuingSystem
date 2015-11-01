package asl.benchmark.database;

import asl.benchmark.*;
import asl.util.CustomLogger;

import java.sql.CallableStatement;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.SQLException;
import org.apache.log4j.Logger;

public class DbSendMessageBenchmark extends BenchmarkTest {
    private static Logger logger = Logger.getLogger(DbSendMessageBenchmark.class);
    private int msgLength;
    private final String content;
    private final int sender;
    private final int queue;
    private final int receiver;
    private CallableStatement statement = null;

    public DbSendMessageBenchmark(Connection connection, int duration, Scanner sc) {
        super(connection, duration);
        logger.info("Type in the length of the messages: ");
        msgLength = sc.nextInt();
        StringBuilder strBuilder = new StringBuilder(msgLength);
        while(msgLength-- > 0) {
            strBuilder.append('x');
        }
        content = strBuilder.toString();

        logger.info("Type in the queue: ");
        queue = sc.nextInt();

        logger.info("Type in the sender: ");
        sender = sc.nextInt();

        logger.info("Type in the receiver: ");
        receiver = sc.nextInt();

        try {
            if(receiver != -1) {
                statement = connection.prepareCall("{ call send_message(?, ?, ?, ?, ?) }");
                statement.setInt(5, receiver);
            } else {
                statement = connection.prepareCall("{ call send_message(?, ?, ?, ?) }");
            }

            statement.setInt(1, sender);
            statement.setString(2, content);
            statement.setInt(3, queue);
            statement.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));
        } catch (SQLException e) {
            logger.error("FAILED to prepare statement");
        }
    }

    @Override
    public void run() {
        double seconds = duration / 1000;
        double successfulResponsesCount = 0.0;
        long current = System.currentTimeMillis();
        long start = current;
        long end = current + duration;
        long operationStart, responseTime;
        boolean isSuccessful;
        CustomLogger dataLogger = new CustomLogger("db", toString());
        while(current <= end) {
            operationStart = System.currentTimeMillis();
            try {
                isSuccessful = (statement.executeUpdate() == 0);
            } catch (SQLException e) {
                logger.error("FAILED to execute statement");
                isSuccessful = false;
            }
            responseTime = System.currentTimeMillis() - operationStart;
            if(isSuccessful) {
                successfulResponsesCount += 1.0;
                dataLogger.println( (operationStart - start) + " " + responseTime);
            } else {
                logger.info("Unsuccessful attempt");
            }

            current = System.currentTimeMillis();
        }
        dataLogger.println("-1 " + successfulResponsesCount/seconds);
        dataLogger.flush();
    }
}