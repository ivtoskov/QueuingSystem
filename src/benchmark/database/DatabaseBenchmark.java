package asl.benchmark.database;

import asl.benchmark.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.DriverManager;
import org.apache.log4j.Logger;

public class DatabaseBenchmark {
    private static Logger logger = Logger.getLogger(DatabaseBenchmark.class);

    public static BenchmarkTest prepare(String host, int port, int operationType, int duration, Scanner sc) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/asl" +
                    "", "kennelcrash", "paladin");
        } catch (SQLException e) {
            logger.error("Could not open connection to the database");
            return null;
        }

        switch (operationType) {
            case BenchmarkExecutor.SEND_MESSAGE:
                return new DbSendMessageBenchmark(connection, duration, sc);
            case BenchmarkExecutor.POP_QUEUE:
                return new DbPopQueueBenchmark(connection, duration, sc);
            case BenchmarkExecutor.PEEK_QUEUE:
                return new DbPeekQueueBenchmark(connection, duration, sc);
            case BenchmarkExecutor.POP_SENDER:
                return new DbPopSenderBenchmark(connection, duration, sc);
            case BenchmarkExecutor.PEEK_SENDER:
                return new DbPeekSenderBenchmark(connection, duration, sc);
            default:
                return null;
        }
    }

}