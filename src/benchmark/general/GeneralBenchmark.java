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

/**
 * A factory class that generates test objects
 * based on specified configuration.
 */
public class GeneralBenchmark {
    private static Logger logger = Logger.getLogger(GeneralBenchmark.class);

    /**
     * A method that prepares a benchmark test that should be executed.
     *
     * @param host Host of the database/middleware to connect to.
     * @param port Port of the database/middleware to connect to.
     * @param id The id of the simulated client.
     * @param benchmarkInfo The information accompanying the benchmark.
     * @return A complete test objects that corresponds to a benchmark test.
     */
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