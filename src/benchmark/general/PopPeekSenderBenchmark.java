package asl.benchmark.general;

import asl.middleware.SocketWrapper;
import asl.benchmark.*;
import asl.util.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class PopPeekSenderBenchmark extends BenchmarkTest {
    private final String content;
    private final int id;
    private final List<Command> commands;
    private static Logger logger = Logger.getLogger(PopPeekSenderBenchmark.class);
    public PopPeekSenderBenchmark(SocketWrapper sw, int id, BenchmarkInfo benchmarkInfo) {
        super(sw, benchmarkInfo.getDuration(), benchmarkInfo);
        this.id = id;
        int msgLength = benchmarkInfo.getMessageLength();
        StringBuilder strBuilder = new StringBuilder(msgLength);
        while(msgLength-- > 0) {
            strBuilder.append('x');
        }
        content = strBuilder.toString();

        commands = new ArrayList<Command>();
        if(benchmarkInfo.isCrossSend()) {
            commands.add(new Command(new Message(null, id, id, -1),
                    benchmarkInfo.getOperationType() == BenchmarkExecutor.POP_SENDER ?
                            Command.Type.POP_SENDER : Command.Type.PEEK_SENDER));
        } else {
            int[] senders = benchmarkInfo.getSenders();
            for(int si = 0; si < senders.length; ++si) {
                commands.add(new Command(new Message(null, senders[si], id, -1),
                        benchmarkInfo.getOperationType() == BenchmarkExecutor.POP_SENDER ?
                                Command.Type.POP_SENDER : Command.Type.PEEK_SENDER));
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
        String response = null;
        ObjectOutputStream oos = sw.getOos();
        ObjectInputStream ois = sw.getOis();
        // Warmup
        for (int i = 0; i < 100; i++) {
            try {
                command = commands.get(i % commands.size());
                oos.writeUnshared(command);
                oos.flush();
                response = (String) ois.readUnshared();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        double seconds = duration / 1000.0;
        long current = System.currentTimeMillis();
        long end = current + duration;
        long startOp = 0L, realRp = 0L;
        long thinkBegin = -1L, thinkEnd = 0L;

        while(current <= end) {
            command = commands.get(counter % commands.size());
            ++counter;

            try {
                startOp = System.nanoTime();
                oos.writeUnshared(command);
                oos.flush();
                response = (String) ois.readUnshared();
                realRp = System.nanoTime() - startOp;
                if(response != null && !response.startsWith("FAILED")) {
                    isSuccessful = true;
                } else {
                    isSuccessful = false;
                }
            } catch (IOException | ClassNotFoundException e) {
                isSuccessful = false;
            }

            if(isSuccessful) {
                dataLogger.println(response + "," + String.format("%.2f", realRp / 1000000.0));
            } else {
                logger.info("Unsuccessful attempt");
            }

            current = System.currentTimeMillis();
        }

        dataLogger.flush();
        sw.close();
        dataLogger.close();
    }
}