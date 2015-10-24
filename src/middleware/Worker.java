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

public class Worker implements Runnable {
    private Logger logger = Logger.getLogger(Worker.class);
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public Worker() {}

    @Override
    public void run() {
        SocketWrapper sw = ClientSocketController.get();
        boolean ret;
        while (ClientSocketController.notShutDown) {
            ret = true;
            if(sw == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("The worker got interrupted.");
                }
            } else {
                ret = handle(sw);
                if(ret)
                    ClientSocketController.add(sw);
            }

            sw = ClientSocketController.get();
        }
    }

    public boolean handle(SocketWrapper sw) {
        socket = sw.getSocket();
        ois = sw.getOis();
        oos = sw.getOos();
        ConnectionWrapper cw = null;
        try {
            Command command = (Command) ois.readObject();
            cw = ConnectionPool.get();
            while(cw == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("The worker got interrupted.");
                }
                cw = ConnectionPool.get();
            }
            String response = cw.handle(command);
            if(cw != null && !cw.isCorrupted()) {
                ConnectionPool.add(cw);
                cw = null;
            }
            oos.writeObject(response);
        } catch (EOFException e) {
            logger.error("EOF Exception. Probably the client disconnected.");
            return false;
        } catch (IOException e) {
            logger.error("Problem encountered while connecting to the client or the system has been shut down.");
        } catch (ClassNotFoundException e) {
            logger.error("Invalid response from client.");
        } finally {
            if(cw != null && !cw.isCorrupted())
                ConnectionPool.add(cw);
        }
        return true;
    }
}