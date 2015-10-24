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
            // Cannot handle
        }
    }

    public static synchronized void add(SocketWrapper sw) {
        clientSockets.add(sw);
    }

    public static synchronized SocketWrapper get() {
        int size = clientSockets.size();
        SocketWrapper sw = clientSockets.poll();
        if(size > 0 && size == clientSockets.size()) logger.info("imame problem");
        return sw;
    }

    public static synchronized void shutDown() {
        for(SocketWrapper sw: clientSockets) {
            if(sw != null) {
                sw.close();
            }
        }
        notShutDown = false;
        clientSockets.clear();
    }
}