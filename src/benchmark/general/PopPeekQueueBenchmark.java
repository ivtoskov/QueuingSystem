package asl.benchmark.general;

import asl.middleware.SocketWrapper;
import asl.benchmark.*;
import asl.util.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class PopPeekQueueBenchmark extends BenchmarkTest {
    private final String content;
    private final int id;
    private final List<Command> commands;
    private static Logger logger = Logger.getLogger(PopPeekQueueBenchmark.class);
    public PopPeekQueueBenchmark(SocketWrapper sw, int id, BenchmarkInfo benchmarkInfo) {
        super(sw, benchmarkInfo.getDuration(), benchmarkInfo);
        this.id = id;
        int msgLength = benchmarkInfo.getMessageLength();
        StringBuilder strBuilder = new StringBuilder(msgLength);
        while(msgLength-- > 0) {
            strBuilder.append('x');
        }
        content = strBuilder.toString();

        commands = new ArrayList<Command>();
        if(benchmarkInfo.isCrossQueue()) {
            commands.add(new Command(new Message(null, -1, id, id),
                    benchmarkInfo.getOperationType() == BenchmarkExecutor.POP_QUEUE ?
                            Command.Type.POP_QUEUE : Command.Type.PEEK_QUEUE));
        } else {
            int[] queues = benchmarkInfo.getQueues();
            for(int qi = 0; qi < queues.length; ++qi) {
                commands.add(new Command(new Message(null, -1, id, queues[qi]),
                        benchmarkInfo.getOperationType() == BenchmarkExecutor.POP_QUEUE ?
                                Command.Type.POP_QUEUE : Command.Type.PEEK_QUEUE));
            }
        }
    }

    @Override
    public void run() {
        boolean isSuccessful;
        CustomLogger dataLogger = new CustomLogger("general", toString() + "popPeek");
        int counter = 0;
        int qid, rid;
        Command command;
        ObjectOutputStream oos = sw.getOos();
        ObjectInputStream ois = sw.getOis();
        String response = null;
        double seconds = duration / 1000.0;
        double successfulResponsesCount = 0.0;
        long current = System.currentTimeMillis();
        long end = current + duration;

        while(current <= end) {
            command = commands.get(counter % commands.size());
            ++counter;

            try {
                oos.writeUnshared(command);
                oos.flush();
                response = (String) ois.readUnshared();
                if(response != null && response != "" && !response.startsWith("FAILED")) {
                    isSuccessful = true;
                } else {
                    isSuccessful = false;
                }
            } catch (IOException | ClassNotFoundException e) {
                isSuccessful = false;
            }

            if(isSuccessful) {
                successfulResponsesCount += 1.0;
                dataLogger.println(response);
            } else {
                logger.info("Unsuccessful attempt");
                logger.info(response);
            }

            current = System.currentTimeMillis();
        }

        dataLogger.flush();
        sw.close();
        dataLogger.close();
    }
}