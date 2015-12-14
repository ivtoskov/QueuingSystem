package asl.middleware;

import asl.util.Command;
import asl.util.Message;

import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.InterruptedException;
import java.net.Socket;

/**
 * A worker in a system that handles different
 * requests from the clients.
 */
public class Worker extends Thread {
    private Logger logger = Logger.getLogger(Worker.class);
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public Worker() {}

    @Override
    public void run() {
        logger.info("Starting Worker " + this);
        Node.mt.workers.add(Thread.currentThread());
        boolean ret;
        SocketWrapper sw = null;
        while (Node.notShutDown) {
            try {
                sw = Node.sockets.take();
                sw.getTimeTracker().setMiddlewareWaitingTime();
                ret = handle(sw);
                if(ret && Node.notShutDown) {
                    Node.sockets.put(sw);
                    sw.getTimeTracker().reset();
                } else {
                    if(sw != null) sw.close();
                    Node.socketsToBeClosed.remove(sw);
                }
            } catch (InterruptedException e) {
                logger.error("The worker got interrupted.");
                break;
            }
        }
        logger.info("Terminating Worker " + this);
    }

    public boolean handle(SocketWrapper sw) {
        socket = sw.getSocket();
        ois = sw.getOis();
        oos = sw.getOos();
        ConnectionWrapper cw = null;
        try {
            Command command = (Command) ois.readUnshared();
            try {
                sw.getTimeTracker().setMiddlewareServiceTime();
                cw = Node.connections.take();
                sw.getTimeTracker().setDatabaseWaitingTime();
                String response = cw.handle(command);
                sw.getTimeTracker().setDatabaseServiceTime();
                sw.getTimeTracker().setResponseTime();
                if(cw != null && !cw.isCorrupted()) {
                    Node.connections.put(cw);
                }
                if(response.contains("FAILED")) {
                    oos.writeUnshared(response);
                } else {
                    oos.writeUnshared(sw.getTimeTracker().toString());
                }

                oos.flush();
            } catch (InterruptedException e) {
                logger.error("The worker got interrupted.");
            }
        } catch (EOFException e) {
            logger.error("EOF Exception. Probably the client disconnected.");
            return false;
        } catch (IOException e) {
            logger.error("Problem encountered while connecting to the client or the system has been shut down.");
        } catch (ClassNotFoundException e) {
            logger.error("Invalid response from client.");
        }
        return true;
    }
}