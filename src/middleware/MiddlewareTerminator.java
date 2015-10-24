package asl.middleware;

import java.io.IOException;
import java.lang.Override;
import java.net.ServerSocket;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class MiddlewareTerminator extends Thread {
    private Logger logger = Logger.getLogger(MiddlewareTerminator.class);
    private final ServerSocket server;

    public MiddlewareTerminator(ServerSocket server) {
        this.server = server;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {

        }
        ClientSocketController.shutDown();
        try {
            server.close();
        } catch (IOException e) {
            logger.error("Problem while closing server.");
        }
    }
}