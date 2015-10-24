package asl.middleware;

import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientSocketController {
    private static Logger logger = Logger.getLogger(ClientSocketController.class);
    private static Queue<SocketWrapper> clientSockets = new LinkedList<SocketWrapper>();
    public static boolean notShutDown = true;

    public static synchronized void add(Socket socket) {
        try {
            final ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            final ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            clientSockets.add(new SocketWrapper(socket, ois, oos));
        } catch (IOException e) {
            logger.error("Error while opening a client socket.");
        }
    }

    public static synchronized void add(SocketWrapper sw) {
        clientSockets.add(sw);
    }

    public static synchronized SocketWrapper get() {
        return clientSockets.poll();
    }

    public static synchronized void shutDown() {
        notShutDown = false;
        for(SocketWrapper sw: clientSockets) {
            if(sw != null) {
                sw.close();
            }
        }
        clientSockets.clear();
        ConnectionPool.shutDown();
    }
}