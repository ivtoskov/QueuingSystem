package asl.middleware;

import java.io.IOException;
import java.lang.Override;
import java.lang.Thread;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;


/**
 * A class responsible for the termination of the middleware.
 */
public class MiddlewareTerminator extends Thread {
    private Logger logger = Logger.getLogger(MiddlewareTerminator.class);
    private final ServerSocket server;
    public List<Thread> workers = new ArrayList<Thread>();

    public MiddlewareTerminator(ServerSocket server) {
        this.server = server;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        String s = null;
        try {
            do {
                s = sc.nextLine();
            } while (!"exit".equals(s));
        } catch (java.util.NoSuchElementException e) {

        }
        logger.info("Shutting down system.");

        Node.notShutDown = false;
        for(Thread w : workers) {
            logger.info("Interrupting " + w);
            w.interrupt();
        }

        try {
            server.close();
        } catch (IOException e) {
            logger.error("Problem while closing server.");
        }
    }
}