package asl.middleware;

import org.apache.log4j.Logger;
import java.io.IOException;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.Queue;

public class ConnectionPool {
    private static Logger logger = Logger.getLogger(ConnectionPool.class);
    private static Queue<ConnectionWrapper> connections = new LinkedList<ConnectionWrapper>();
    public static boolean notShutDown = true;

    public static synchronized void add(Connection connection) {
        ConnectionWrapper cw = new ConnectionWrapper(connection);
        if(cw != null && !cw.isCorrupted())
            connections.add(cw);
    }

    public static synchronized void add(ConnectionWrapper cw) {
        connections.add(cw);
    }

    public static synchronized ConnectionWrapper get() {
        return connections.poll();
    }

    public static synchronized void shutDown() {
        notShutDown = false;
        for(ConnectionWrapper cw: connections) {
            if(cw != null) {
                cw.close();
            }
        }
        connections.clear();
    }
}