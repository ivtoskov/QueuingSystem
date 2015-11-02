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
    private final String content;
    private CallableStatement statement = null;

    public DbSendMessageBenchmark(Connection connection, int id, BenchmarkInfo benchmarkInfo) {
        super(connection, benchmarkInfo.getDuration(), benchmarkInfo);
        int msgLength = benchmarkInfo.getMessageLength();
        StringBuilder strBuilder = new StringBuilder(msgLength);
        while(msgLength-- > 0) {
            strBuilder.append('x');
        }
        content = strBuilder.toString();

        try {
            if(benchmarkInfo.isBroadcast()) {
                statement = connection.prepareCall("{ call send_message(?, ?, ?, ?) }");
            } else {
                statement = connection.prepareCall("{ call send_message(?, ?, ?, ?, ?) }");
            }

            if(benchmarkInfo.isCrossQueue()) {
                statement.setInt(3, id);
            }

            if(benchmarkInfo.isCrossSend()) {
                statement.setInt(5, id);
            }

            statement.setInt(1, id);
            statement.setString(2, content);
        } catch (SQLException e) {
            logger.error("FAILED to prepare statement");
        }
    }

    @Override
    public void run() {
        double seconds = duration / 1000.0;
        double successfulResponsesCount = 0.0;
        long current = System.currentTimeMillis();
        long start = current;
        long end = current + duration;
        long operationStart, responseTime;
        boolean isSuccessful;
        CustomLogger dataLogger = new CustomLogger("db", toString());
        int counter = 0;
        while(current <= end) {
            try {
                if(!benchmarkInfo.isCrossQueue())
                    statement.setInt(3, benchmarkInfo.getQueues()[counter % benchmarkInfo.getQueues().length]);

                statement.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));

                if(!benchmarkInfo.isCrossSend() && !benchmarkInfo.isBroadcast())
                    statement.setInt(5, benchmarkInfo.getReceivers()[counter % benchmarkInfo.getReceivers().length]);

                ++counter;
            } catch (SQLException e) {
                logger.error("Error while setting parameters.");
                continue;
            }
            operationStart = System.currentTimeMillis();
            try {
                statement.execute();
                isSuccessful = true;
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