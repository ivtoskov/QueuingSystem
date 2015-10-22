package asl.client;

import asl.util.Command;
import asl.util.Message;

import java.lang.Integer;
import java.util.Scanner;

public class SendBroadcastCommandBuilder extends CommandBuilder {
    public SendBroadcastCommandBuilder() {
        type = Command.Type.SEND_BROADCAST;
    }

    public Command createCommand(Scanner sc, int sid, String args[]) {
        int qid = Integer.parseInt(args[2]);
        String content = sc.nextLine();
        Message msg = new Message(content, sid, qid);
        return new Command(msg, type);
    }
}