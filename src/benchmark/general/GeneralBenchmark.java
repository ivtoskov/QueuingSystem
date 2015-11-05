package asl.benchmark.general;

import asl.benchmark.*;
import asl.middleware.SocketWrapper;

import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.log4j.Logger;

public class GeneralBenchmark {
    private static Logger logger = Logger.getLogger(GeneralBenchmark.class);

    public static BenchmarkTest prepare(String host, int port, int id, BenchmarkInfo benchmarkInfo) {
        Socket socket = null;
        SocketWrapper sw = null;
        try {
            socket = new Socket(host, port);
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            sw = new SocketWrapper(socket, ois, oos);
        } catch (IOException e) {
            logger.error("Error while connecting to the middleware.");
            return null;
        }

        switch (benchmarkInfo.getOperationType()) {
            case BenchmarkExecutor.SEND_MESSAGE:
                return new SendMessageBenchmark(sw, id, benchmarkInfo);
            case BenchmarkExecutor.POP_QUEUE:
            case BenchmarkExecutor.PEEK_QUEUE:
                return new PopPeekQueueBenchmark(sw, id, benchmarkInfo);
            case BenchmarkExecutor.POP_SENDER:
            case BenchmarkExecutor.PEEK_SENDER:
                return new PopPeekSenderBenchmark(sw, id, benchmarkInfo);
            default:
                return null;
        }
    }

}