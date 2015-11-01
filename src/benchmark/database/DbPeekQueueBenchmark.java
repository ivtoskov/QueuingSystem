package asl.benchmark.database;

import asl.benchmark.*;
import asl.util.CustomLogger;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import java.sql.Types;

public class DbPeekQueueBenchmark extends BenchmarkTest {
    private static Logger logger = Logger.getLogger(DbPeekQueueBenchmark.class);
    private final int queue;
    private final int receiver;
    private CallableStatement statement = null;

    public DbPeekQueueBenchmark(Connection connection, int duration, Scanner sc) {
        super(connection, duration);

        logger.info("Type in the sender: ");
        queue = sc.nextInt();

        logger.info("Type in the receiver: ");
        receiver = sc.nextInt();

        try {
            statement = connection.prepareCall("{ ? = call peek_queue( ?, ? ) }");
            statement.registerOutParameter(1, Types.VARCHAR);
            statement.setInt(2, receiver);
            statement.setInt(3, queue);
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
    }
}