package asl.middleware;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SocketWrapper {
    private final Socket socket;
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    public SocketWrapper(Socket socket, ObjectInputStream ois, ObjectOutputStream oos) {
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
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

    public void close() {
        if(socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                // Cannot handle
            }
        }

        if(ois != null) {
            try {
                ois.close();
            } catch (IOException e) {
                // Cannot handle
            }
        }

        if(oos != null) {
            try {
                oos.close();
            } catch (IOException e) {
                // Cannot handle
            }
        }
    }
}