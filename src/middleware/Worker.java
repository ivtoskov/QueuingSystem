package asl.middleware;

import asl.util.Command;
import asl.util.Message;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

public class Worker implements Runnable {
    private Logger logger = Logger.getLogger(Worker.class);
    private final Socket socket;

    public Worker(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            Command command = (Command) ois.readObject();
        } catch (IOException e) {
            logger.error("Problem encountered while connecting to the client.");
        } catch (ClassNotFoundException e) {
            logger.error("Invalid response from client.");
        } finally {
            if(socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Cannot handle
                }
            }
        }
    }
}