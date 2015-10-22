package asl.middleware;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node {
    private static Logger logger = Logger.getLogger(Node.class);

    public static void main(String args[]) {
        int port = -1;

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.error("Usage: java -jar Node <port>");
            System.exit(1);
        }

        ExecutorService executor = null;
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            executor = Executors.newFixedThreadPool(20);
            serverSocket = new ServerSocket(port);
            while (true) {
                clientSocket = serverSocket.accept();
                executor.execute(new Worker(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Input/output error encountered.");
        } finally {
            close(executor, serverSocket);
        }
    }

    private static void close(ExecutorService executor, ServerSocket serverSocket) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // Cannot handle it
            }
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}