package asl.benchmark;

import java.lang.System;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.log4j.Logger;
import asl.benchmark.database.*;

/**
 * Class that is used to execute different benchmarks.
 */
public class BenchmarkExecutor {
    private static Scanner sc;
    private static Logger logger = Logger.getLogger(BenchmarkExecutor.class);
    private static List<BenchmarkTest> testInstances;
    public static final int POP_QUEUE = 1;
    public static final int PEEK_QUEUE = 2;
    public static final int POP_SENDER = 3;
    public static final int PEEK_SENDER = 4;
    public static final int SEND_MESSAGE = 5;

    public static void main(String[] args) {
        int port = -1;
        String host = "";

        // Parse host and port of the SUT
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.error("Usage: java -jar BenchmarkExecutor.jar <host> <port>");
            System.exit(1);
        }

        testInstances = new ArrayList<BenchmarkTest>();
        sc = new Scanner(System.in);
        // Specify the 'System under test'
        logger.info("Please select the SUT of the benchmark: ");
        logger.info("1) database");
        logger.info("2) middleware");
        logger.info("3) whole system");
        int sutType = sc.nextInt();

        // Specify the duration of the test
        logger.info("Please specify the duration of the test in seconds: ");
        int duration = sc.nextInt();

        while (true) {
            // Specify the type of operation to test
            logger.info("Starting the definition of a new test...");
            logger.info("Please select the operation you want to perform: ");
            logger.info("1) pop queue");
            logger.info("2) peek queue");
            logger.info("3) pop sender");
            logger.info("4) peek sender");
            logger.info("5) send message");
            logger.info("----------------");
            logger.info("6) to run the experiment");
            int operationType = sc.nextInt();
            if (operationType == 6) break;

            switch (sutType) {
                case 1:
                    testInstances.add(DatabaseBenchmark.prepare(host, port, operationType, duration, sc));
                    break;
            }

        }

        logger.info("The experiment is ready. Please press ENTER to run it.");
        sc.nextLine();
        sc.nextLine();
        logger.info("Starting the experiment...");

        for(BenchmarkTest instance : testInstances) {
            (new Thread(instance)).start();
        }

        logger.info("Experiment successfully started.");

        sc.close();
    }
}