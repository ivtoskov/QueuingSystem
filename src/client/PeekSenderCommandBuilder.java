package asl.client;

import asl.util.Command;
import asl.util.Message;

import java.lang.Integer;
import java.util.Scanner;

public class PeekSenderCommandBuilder extends CommandBuilder {
    public PeekSenderCommandBuilder() {
        type = Command.Type.PEEK_SENDER;
    }

    public Command createCommand(Scanner sc, int sid, String args[]) {
        int sender = Integer.parseInt(args[2]);
        Message msg = new Message(null, sender, sid, -1);
        return new Command(msg, type);
    }
}