package asl.client;

import asl.util.Command;
import asl.util.Message;

import java.lang.Integer;
import java.util.Scanner;

public class SendToCommandBuilder extends CommandBuilder {
    public SendToCommandBuilder() {
        type = Command.Type.SEND_TO;
    }

    public Command createCommand(Scanner sc, int sid, String args[]) {
        int qid = Integer.parseInt(args[2]);
        int rid = Integer.parseInt(args[3]);
        String content = sc.nextLine();
        Message msg = new Message(content, sid, rid, qid);
        return new Command(msg, type);
    }
}