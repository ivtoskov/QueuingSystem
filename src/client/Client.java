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

    Scanner sc = new Scanner(System.in);
    Map<String, CommandBuilder> commandsMap = initializeCommands();
    while(sc.hasNext()) {
      try {
        String commandArgs[] = sc.nextLine().split("\\s+");
        Command command = commandsMap.get(commandArgs[0]+commandArgs[1]).createCommand(sc, id, commandArgs);
        sendCommand(command, host, port);
      } catch (ArrayIndexOutOfBoundsException | NumberFormatException | NullPointerException |NoSuchElementException|
      IllegalStateException e) { // TODO
        logger.error("Invalid command");
      }
    }
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

  private static void sendCommand(Command c, String host, int port) {
    try(Socket socket = new Socket(host, port);
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());) {
      oos.writeObject(c);
      Message response = (Message) ois.readObject();
    } catch (UnknownHostException e) {
      logger.error("Cannot connect to host: " + host);
    } catch (IOException e) {
      logger.error("Problem encountered while connecting to the server.");
    } catch (ClassNotFoundException e) {
      logger.error("Invalid response from server.");
    }
  }
}
