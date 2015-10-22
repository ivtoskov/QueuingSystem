package asl.client;

import asl.util.Command;
import asl.util.Message;

import java.lang.Integer;
import java.util.Scanner;

public class QueryQueueCommandBuilder extends CommandBuilder {
    public QueryQueueCommandBuilder() {
        type = Command.Type.QUERY_QUEUE;
    }

    public Command createCommand(Scanner sc, int sid, String args[]) {
        Message msg = new Message(null, -1, sid, -1);
        return new Command(msg, type);
    }
}