package asl.middleware;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Node {
    private static Logger logger = Logger.getLogger(Node.class);

    public static void main(String args[]) {
        int port = -1;
        String host = "localhost";
        int databasePort = 5432;

        try {
            port = Integer.parseInt(args[0]);
            if(args.length > 1)
                host = args[1];

            if(args.length > 2)
                databasePort = Integer.parseInt(args[2]);

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.error("Usage: java -jar Node <port>");
            System.exit(1);
        }

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            initConnections(host, databasePort);
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
            (new Thread(w)).start();
        }
    }

    private static void initConnections(String host, int port) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Cannot load a driver for PostgreSQL.");
            return;
        }

        int numberOfWorkers = Runtime.getRuntime().availableProcessors() / 4;
        ++numberOfWorkers;
        while(numberOfWorkers-- > 0) {
            try {
                Connection c = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/ASL",
                        "kennelcrash", "paladin");
                ConnectionPool.add(c);
            } catch (SQLException e) {
                logger.error("Error while opening a connection.");
                e.printStackTrace();
            }
        }
    }
}