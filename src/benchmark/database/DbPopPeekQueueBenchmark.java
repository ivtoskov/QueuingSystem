package asl.benchmark.database;

import asl.benchmark.*;
import asl.util.CustomLogger;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import java.sql.Types;

public class DbPopPeekQueueBenchmark extends BenchmarkTest {
    private static Logger logger = Logger.getLogger(DbPopPeekQueueBenchmark.class);
    private CallableStatement statement = null;

    public DbPopPeekQueueBenchmark(Connection connection, int id, BenchmarkInfo benchmarkInfo) {
        super(connection, benchmarkInfo.getDuration(), benchmarkInfo);

        try {
            if(benchmarkInfo.getOperationType() == BenchmarkExecutor.POP_QUEUE)
                statement = connection.prepareCall("{ ? = call pop_queue( ?, ? ) }");
            else
                statement = connection.prepareCall("{ ? = call peek_queue( ?, ? ) }");

            statement.registerOutParameter(1, Types.VARCHAR);
            statement.setInt(2, id);
            if(benchmarkInfo.isCrossQueue())
                statement.setInt(3, id);
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
        CustomLogger dataLogger = new CustomLogger("db", toString() + "popPeekQueue");
        int counter = 0;
        while(current <= end) {
            try {
                if(!benchmarkInfo.isCrossQueue())
                    statement.setInt(3, benchmarkInfo.getQueues()[counter % benchmarkInfo.getQueues().length]);

                ++counter;
            } catch (SQLException e) {
                logger.error("Error while preparing statement.");
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
        dataLogger.println("-1 " + successfulResponsesCount / seconds);
        dataLogger.flush();
        try {
            if(statement != null && !statement.isClosed()) statement.close();
            if(connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            logger.error("Error while closing statement/connection.");
        }
        dataLogger.close();
    }
}