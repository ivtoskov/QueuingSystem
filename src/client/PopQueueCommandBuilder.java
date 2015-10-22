package asl.client;

import asl.util.Command;
import asl.util.Message;

import java.lang.Integer;
import java.util.Scanner;

public class PopQueueCommandBuilder extends CommandBuilder {
    public PopQueueCommandBuilder() {
        type = Command.Type.POP_QUEUE;
    }

    public Command createCommand(Scanner sc, int sid, String args[]) {
        int qid = Integer.parseInt(args[2]);
        Message msg = new Message(null, -1, sid, qid);
        return new Command(msg, type);
    }
}