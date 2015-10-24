package asl.middleware;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            initWorkers();
            (new Thread(new MiddlewareTerminator(serverSocket))).start();
            while (ClientSocketController.notShutDown) {
                clientSocket = serverSocket.accept();
                ClientSocketController.add(clientSocket);
            }
        } catch (IOException e) {
            logger.error("Input/output error encountered or the system has been shut down.");
        } finally {
            close(serverSocket);
        }
    }

    private static void close(ServerSocket serverSocket) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // Cannot handle it
            }
        }
        ClientSocketController.shutDown();
    }

    private static void initWorkers() {
        int numberOfWorkers = Runtime.getRuntime().availableProcessors();
        while(numberOfWorkers-- > 0) {
            Worker w = new Worker();
            logger.info(w);
            (new Thread(w)).start();
        }
    }
}