package asl.client;

import org.apache.log4j.Logger;

import asl.util.Message;
import asl.util.Command;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
  private static Logger logger = Logger.getLogger(Client.class);
  private static Socket socket;
  private static ObjectInputStream ois;
  private static ObjectOutputStream oos;

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
    try {
      socket = new Socket(host, port);
      oos = new ObjectOutputStream(socket.getOutputStream());
      oos.flush();
      ois = new ObjectInputStream(socket.getInputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }

    Scanner sc = new Scanner(System.in);
    Map<String, CommandBuilder> commandsMap = initializeCommands();
    while(sc.hasNext()) {
      try {
        String commandArgs[] = sc.nextLine().split("\\s+");
        Command command = commandsMap.get(commandArgs[0]+commandArgs[1]).createCommand(sc, id, commandArgs);
        oos.writeObject(command);
        Message response = (Message) ois.readObject();
        logger.info(response.getContent()); // TODO
      } catch (ArrayIndexOutOfBoundsException | NumberFormatException | NullPointerException |NoSuchElementException|
      IllegalStateException e) {
        e.printStackTrace();
        logger.error("Invalid command");
      } catch (IOException | ClassNotFoundException e) {
        logger.error("Connection error");
      }
    }
    close();
  }

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

  private static void close() {
    if(socket != null && !socket.isClosed()){
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
