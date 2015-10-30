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
                ret = handle(sw);
                if(ret && Node.notShutDown) {
                    Node.sockets.put(sw);
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
                cw = Node.connections.take();
                String response = cw.handle(command);
                if(cw != null && !cw.isCorrupted()) {
                    Node.connections.put(cw);
                }
                oos.writeUnshared(response);
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