package asl.benchmark.database;

import asl.benchmark.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.DriverManager;
import org.apache.log4j.Logger;

public class DatabaseBenchmark {
    private static Logger logger = Logger.getLogger(DatabaseBenchmark.class);

    public static BenchmarkTest prepare(String host, int port, int id, BenchmarkInfo benchmarkInfo) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/asl" +
                    "", "kennelcrash", "paladin");
        } catch (SQLException e) {
            logger.error("Could not open connection to the database");
            return null;
        }

        switch (benchmarkInfo.getOperationType()) {
            case BenchmarkExecutor.SEND_MESSAGE:
                return new DbSendMessageBenchmark(connection, id, benchmarkInfo);
            case BenchmarkExecutor.POP_QUEUE:
            case BenchmarkExecutor.PEEK_QUEUE:
                return new DbPopPeekQueueBenchmark(connection, id, benchmarkInfo);
            case BenchmarkExecutor.POP_SENDER:
            case BenchmarkExecutor.PEEK_SENDER:
                return new DbPopPeekSenderBenchmark(connection, id, benchmarkInfo);
            default:
                return null;
        }
    }

}