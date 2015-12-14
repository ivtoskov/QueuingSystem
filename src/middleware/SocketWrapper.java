package asl.middleware;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.log4j.Logger;
import asl.util.TimeTracker;

/**
 * A wrapper class that ties together a socket and its
 * corresponding ObjectInput- and ObjectOutputStream.
 */
public class SocketWrapper {
    private static Logger logger = Logger.getLogger(SocketWrapper.class);
    private final Socket socket;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;
    private final TimeTracker timeTracker;

    public SocketWrapper(Socket socket, ObjectInputStream ois, ObjectOutputStream oos) {
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
        this.timeTracker = new TimeTracker();
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getOis() {
        return ois;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public TimeTracker getTimeTracker() {
        return timeTracker;
    }

    public void close() {
        if(socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error while closing socket.");
            }
        }

        if(ois != null) {
            try {
                ois.close();
            } catch (IOException e) {
                logger.error("Error while closing input stream.");
            }
        }

        if(oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                logger.error("Error while closing output stream.");
            }
        }
    }
}