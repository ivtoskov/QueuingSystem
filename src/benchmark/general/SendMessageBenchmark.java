package asl.benchmark.general;

import asl.middleware.SocketWrapper;
import asl.benchmark.*;
import asl.util.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class SendMessageBenchmark extends BenchmarkTest {
    private final String content;
    private final int id;
    private final List<Command> commands;
    private static Logger logger = Logger.getLogger(SendMessageBenchmark.class);
    public SendMessageBenchmark(SocketWrapper sw, int id, BenchmarkInfo benchmarkInfo) {
        super(sw, benchmarkInfo.getDuration(), benchmarkInfo);
        this.id = id;
        int msgLength = benchmarkInfo.getMessageLength();
        StringBuilder strBuilder = new StringBuilder(msgLength);
        while(msgLength-- > 0) {
            strBuilder.append('x');
        }
        content = strBuilder.toString();

        commands = new ArrayList<Command>();
        if(!benchmarkInfo.isCrossQueue()) {
            int[] queues = benchmarkInfo.getQueues();
            if(!benchmarkInfo.isCrossSend() && !benchmarkInfo.isBroadcast()) {
                int[] receivers =benchmarkInfo.getReceivers();
                for(int qi = 0; qi < queues.length; ++qi) {
                    for(int ri = 0; ri < receivers.length; ++ri) {
                        commands.add(new Command(new Message(content, id, receivers[ri], queues[qi]),
                                Command.Type.SEND_TO));
                    }
                }
            } else {
                if(benchmarkInfo.isBroadcast()) {
                    for(int qi = 0; qi < queues.length; ++qi) {
                        commands.add(new Command(new Message(content, id, queues[qi]),
                                Command.Type.SEND_BROADCAST));
                    }
                } else {
                    for(int qi = 0; qi < queues.length; ++qi) {
                        commands.add(new Command(new Message(content, id, id, queues[qi]),
                                Command.Type.SEND_TO));
                    }
                }
            }
        } else {
            if(!benchmarkInfo.isCrossSend() && !benchmarkInfo.isBroadcast()) {
                int[] receivers = benchmarkInfo.getReceivers();
                for(int ri = 0; ri < receivers.length; ++ri) {
                    commands.add(new Command(new Message(content, id, receivers[ri], id),
                            Command.Type.SEND_TO));
                }
            } else {
                if(benchmarkInfo.isBroadcast()) {
                        commands.add(new Command(new Message(content, id, id),
                                Command.Type.SEND_BROADCAST));
                } else {
                        commands.add(new Command(new Message(content, id, id, id),
                                Command.Type.SEND_TO));
                }
            }
        }
    }

    @Override
    public void run() {
        boolean isSuccessful;
        CustomLogger dataLogger = new CustomLogger("general", toString() + "sendMessage");
        int counter = 0;
        int qid, rid;
        Command command;
        ObjectOutputStream oos = sw.getOos();
        ObjectInputStream ois = sw.getOis();
        long operationStart = 0, responseTime = 0;
        double seconds = duration / 1000.0;
        double successfulResponsesCount = 0.0;
        long current = System.currentTimeMillis();
        long start = current;
        long end = current + duration;

        while(current <= end) {
            command = commands.get(counter % commands.size());
            ++counter;

            try {
                operationStart = System.currentTimeMillis();
                oos.writeUnshared(command);
                oos.flush();
                String response = (String) ois.readUnshared();
                responseTime = System.currentTimeMillis() - operationStart;
                if("SUCCESSFULLY sent message".equals(response)) {
                    isSuccessful = true;
                } else {
                    isSuccessful = false;
                }
            } catch (IOException | ClassNotFoundException e) {
                isSuccessful = false;
            }

            if(isSuccessful) {
                successfulResponsesCount += 1.0;
                dataLogger.println( (operationStart - start) + " " + responseTime);
            } else {
                logger.info("Unsuccessful attempt");
            }

            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {}

            current = System.currentTimeMillis();
        }

        dataLogger.println("-1 " + successfulResponsesCount/seconds);
        dataLogger.flush();
        sw.close();
        dataLogger.close();
    }
}