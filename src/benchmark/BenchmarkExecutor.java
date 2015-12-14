package asl.benchmark;

import java.lang.System;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.log4j.Logger;
import asl.benchmark.database.*;
import asl.benchmark.general.*;

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
        logger.info("2) whole system");
        int sutType = 2; //sc.nextInt();

        // Specify the duration of the test
        logger.info("Please specify the duration of the test in seconds: ");
        int duration = 30; // sc.nextInt();

        boolean firstTime = true;
        BenchmarkInfo benchmarkInfo;
        while (true) {
            benchmarkInfo = new BenchmarkInfo();
            benchmarkInfo.setDuration(duration);
            // Specify the type of operation to test
            int operationType = -1;
            if(firstTime) {
                logger.info("Starting the definition of a new test...");
                logger.info("Please select the operation you want to perform: ");
                logger.info("1) pop queue");
                logger.info("2) peek queue");
                logger.info("3) pop sender");
                logger.info("4) peek sender");
                logger.info("5) send message");
                logger.info("----------------");
                logger.info("6) to run the experiment");
                operationType = sc.nextInt();
                if (operationType == 6) break;
                benchmarkInfo.setOperationType(operationType);
                firstTime = false;
            } else {
                break;
            }

            logger.info("Please type in the number of clients you want to start: ");
            int numOfClients = 15; //sc.nextInt();

            logger.info("Please type in the offset of the id's: ");
            int offset = 1; //sc.nextInt();

            switch (operationType) {
                case SEND_MESSAGE:
                    logger.info("Please type in the length of the messages: ");
                    int msgLength = 200; //sc.nextInt();
                    benchmarkInfo.setMessageLength(msgLength);
                    logger.info("Type in 1 if you want to cross send, 0 if you want to broadcast: ");
                    int broadCastOrCrossSend = 2; //sc.nextInt();
                    if(broadCastOrCrossSend == 1) benchmarkInfo.setCrossSend(true);
                    else if(broadCastOrCrossSend == 0) benchmarkInfo.setBroadcast(true);
                    else {
                        logger.info("Type in the number of receivers: ");
                        int numOfReceivers = numOfClients; //sc.nextInt();
                        int[] receivers = new int[numOfReceivers];
                        logger.info("Type in the first receiver: ");
                        int firstReceiver = 1;//sc.nextInt();
                        for(int i = 0; i < numOfReceivers; ++i) {
                            receivers[i] = firstReceiver + i;
                        }
                        benchmarkInfo.setReceivers(receivers);
                    }
                case POP_QUEUE:
                case PEEK_QUEUE:
                    logger.info("Type in the number of queues(0 to cross queue): ");
                    int numOfQueues = 10; //sc.nextInt();
                    if(numOfQueues == 0) benchmarkInfo.setCrossQueue(true);
                    else {
                        int[] queues = new int[numOfQueues];
                        logger.info("Type in the first queue: ");
                        int firstQueue = 1; //sc.nextInt();
                        for(int i = 0; i < numOfQueues; ++i) {
                            queues[i] = firstQueue + i;
                        }
                        benchmarkInfo.setQueues(queues);
                    }
                    break;
                case POP_SENDER:
                case PEEK_SENDER:
                    logger.info("Type in the number of senders to peek/pop from(0 for cross pop/peek): ");
                    int numOfSenders = numOfClients; //sc.nextInt();
                    if(numOfSenders == 0) benchmarkInfo.setCrossSend(true);
                    else {
                        int[] senders = new int[numOfSenders];
                        logger.info("Type in the first sender: ");
                        int firstSender = 1;//sc.nextInt();
                        for(int i = 0; i < numOfSenders; ++i) {
                            senders[i] = firstSender + i;
                        }
                        benchmarkInfo.setSenders(senders);
                    }
                    break;
            }

            if(sutType == 1)
                for(int i = 0; i < numOfClients; ++i) {
                    testInstances.add(DatabaseBenchmark.prepare(host, port, offset + i, benchmarkInfo));
                }
            else if(sutType == 2)
                for(int i = 0; i < numOfClients; ++i) {
                    testInstances.add(GeneralBenchmark.prepare(host, port, offset + i, benchmarkInfo));
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