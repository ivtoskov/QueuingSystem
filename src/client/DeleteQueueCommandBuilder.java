package asl.client;

import asl.util.Command;
import asl.util.Message;

import java.lang.Integer;
import java.util.Scanner;

public class DeleteQueueCommandBuilder extends CommandBuilder {
    public DeleteQueueCommandBuilder() {
        type = Command.Type.DELETE_QUEUE;
    }

    public Command createCommand(Scanner sc, int sid, String args[]) {
        int qid = Integer.parseInt(args[2]);
        Message msg = new Message(null, sid, qid);
        return new Command(msg, type);
    }
}