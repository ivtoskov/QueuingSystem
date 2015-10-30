package asl.client;

import org.apache.log4j.Logger;

import asl.util.Message;
import asl.util.Command;

import java.io.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A client class that reads commands from the standard input
 * and sends them to the middleware.
 */
public class Client {
  private static Logger logger = Logger.getLogger(Client.class);
  private static Socket socket;
  private static ObjectInputStream ois;
  private static ObjectOutputStream oos;
  private static Scanner sc;

  public static void main(String[] args) {
    int id = -1, port = -1;
    String host = "";

    // Parse host and port of the middleware and id of the client.
    try {
      host = args[0];
      port = Integer.parseInt(args[1]);
      id = Integer.parseInt(args[2]);
    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
      logger.error("Usage: java -jar Client <host> <port> <client-id>");
      System.exit(1);
    }

    // Try to open a connection to the middleware
    try {
      socket = new Socket(host, port);
      oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      oos.flush();
      ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
    } catch (IOException e) {
      logger.error("Error occurred while connecting to the middleware.");
    }

    // Initialize scanner to parse user input
    sc = new Scanner(System.in);
    // Initialize a map with different command handlers
    Map<String, CommandBuilder> commandsMap = initializeCommands();
    while(sc.hasNextLine()) {
      try {
        // Build a command read from standard input
        String commandArgs[] = sc.nextLine().split("\\s+");
        Command command = commandsMap.get(commandArgs[0]+commandArgs[1]).createCommand(sc, id, commandArgs);

        // Send the command to the middleware
        oos.writeUnshared(command);
        oos.flush();

        // Read the response and calculate the elapsed time
        long start = System.currentTimeMillis();
        String response = (String) ois.readUnshared();
        long end = System.currentTimeMillis();
        logger.info(response + " Responsetime: " + (end-start) + "ms");
      } catch (ArrayIndexOutOfBoundsException | NumberFormatException | NullPointerException |NoSuchElementException|
      IllegalStateException e) {
        logger.error("Invalid command");
      } catch (IOException | ClassNotFoundException e) {
        logger.error("Connection error");
      }
    }
    close();
  }

  /**
   * Initialize a map containing a string key that describes a command
   * and a CommandBuilder value that represents the corresponding command handler.
   *
   * @return A Map containing a command handler for each possible command.
   */
  private static Map<String, CommandBuilder> initializeCommands() {
    Map<String, CommandBuilder> commandsMap = new HashMap<String, CommandBuilder>();
    commandsMap.put("deletequeue", new DeleteQueueCommandBuilder());
    commandsMap.put("createqueue", new CreateQueueCommandBuilder());
    commandsMap.put("popqueue", new PopQueueCommandBuilder());
    commandsMap.put("peekqueue", new PeekQueueCommandBuilder());
    commandsMap.put("sendto", new SendToCommandBuilder());
    commandsMap.put("sendbroadcast", new SendBroadcastCommandBuilder());
    commandsMap.put("popsender", new PopSenderCommandBuilder());
    commandsMap.put("peeksender", new PeekSenderCommandBuilder());
    commandsMap.put("queryqueue", new QueryQueueCommandBuilder());
    return commandsMap;
  }

  /**
   * Safely free the unclosed resources.
   */
  private static void close() {
    if(socket != null && !socket.isClosed()){
      try {
        socket.close();
      } catch (IOException e) {
        logger.error("Cannot close server socket.");
      }
    }

    if(ois != null) {
      try {
        ois.close();
      } catch (IOException e) {
        logger.error("Cannot close server input stream.");
      }
    }

    if(oos != null) {
      try {
        oos.close();
      } catch (IOException e) {
        logger.error("Cannot close server output stream.");
      }
    }

    if(sc != null) {
      sc.close();
    }
  }
}
