package asl.middleware;

import org.apache.log4j.Logger;

import java.io.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.InterruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The main middleware node class that is initializing the needed
 * resources and accepts incoming client connections.
 */
public class Node {
    private static Logger logger = Logger.getLogger(Node.class);
    private static int NUM_OF_CONNECTIONS;
    private static int NUM_OF_WORKERS;
    public static BlockingQueue<SocketWrapper> sockets;
    public static BlockingQueue<ConnectionWrapper> connections;
    public static boolean notShutDown;
    public static MiddlewareTerminator mt;
    public static List<SocketWrapper> socketsToBeClosed;

    public static void main(String args[]) {
        NUM_OF_CONNECTIONS = 15;
        NUM_OF_WORKERS = 30;
        notShutDown = true;
        int port = -1;
        String host = "localhost";
        int databasePort = 5432;


        // Parse command line arguments.
        try {
            port = Integer.parseInt(args[0]);
            if(args.length > 1)
                host = args[1];

            if(args.length > 2)
                databasePort = Integer.parseInt(args[2]);

            // Needed only to test the max throughput
            // NUM_OF_WORKERS = Integer.parseInt(args[3]);
            // NUM_OF_CONNECTIONS = Integer.parseInt(args[4]);

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.error("Usage: java -jar Node <port> [<dbhost>] [<dbport>]");
            System.exit(1);
        }

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            // Initialize a blocking queue for the incomming client connections
            sockets = new ArrayBlockingQueue<SocketWrapper>(NUM_OF_CONNECTIONS * 5);
            // Initialize a list with client sockets that need to be closed after node's termination
            socketsToBeClosed = new ArrayList<SocketWrapper>();
            // Initialize a listener server socket
            serverSocket = new ServerSocket(port);
            // Initialize a blocking queue for the connections to the database
            connections = new ArrayBlockingQueue<ConnectionWrapper>(
                    NUM_OF_CONNECTIONS);
            // Open the connections to the database
            initConnections(host, databasePort);
            // Start a thread that is responsible for the proper termination of the middleware node
            mt = new MiddlewareTerminator(serverSocket);
            // Initialize the worker threads that will handle the incoming client connections.
            initWorkers();
            // Start the middleware terminator thread
            (new Thread(mt)).start();
            // Loop until the node is being shut down by the middleware terminator
            while (notShutDown) {
                clientSocket = serverSocket.accept();
                try {
                    final ObjectOutputStream oos = new ObjectOutputStream(
                            new BufferedOutputStream(clientSocket.getOutputStream()));
                    oos.flush();
                    final ObjectInputStream ois = new ObjectInputStream(
                            new BufferedInputStream(clientSocket.getInputStream()));
                    SocketWrapper sw = new SocketWrapper(clientSocket, ois, oos);
                    socketsToBeClosed.add(sw);
                    sockets.put(sw);
                } catch (IOException | InterruptedException e) {
                    logger.error("Error while establishing connection with the client.");
                }
            }
        } catch (IOException e) {
            logger.error("Input/output error encountered or the system has been shut down.");
        } finally {
            close(serverSocket);
        }
    }

    /**
     * Method for proper cleanup of the resources.
     *
     * @param serverSocket - The server socket that should be closed.
     */
    private static void close(ServerSocket serverSocket) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Error while trying to close server socket.");
            }
        }

        for(SocketWrapper sw: socketsToBeClosed) {
            if(sw != null) {
                sw.close();
            }
        }
    }

    /**
     * Initializes the necessary number of workers for this middleware node.
     */
    private static void initWorkers() {
        int numberOfWorkers = NUM_OF_WORKERS;
        while(numberOfWorkers-- > 0) {
            (new Thread(new Worker())).start();
        }
    }

    /**
     * Initializes the necessary number of database connections for this middleware node.
     */
    private static void initConnections(String host, int port) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Cannot load a driver for PostgreSQL.");
            return;
        }

        int numOfConnections = NUM_OF_CONNECTIONS;
        while(numOfConnections-- > 0) {
            try {
                Connection c = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/asl" +
                                "",
                        "kennelcrash", "paladin");
                connections.put(new ConnectionWrapper(c));
            } catch (SQLException | InterruptedException e) {
                logger.error("Error while opening a connection.");
            }
        }
    }
}