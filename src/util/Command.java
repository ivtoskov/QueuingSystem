package asl.util;

import java.io.Serializable;

/**
 * A class that corresponds to the different commands
 * supported by the middleware.
 */
public class Command implements Serializable {
    private final Message msg;
    private final Command.Type type;

    public Command(Message msg, Command.Type type) {
        this.msg = msg;
        this.type = type;
    }

    public Message getMsg() {
        return msg;
    }

    public asl.util.Command.Type getType() {
        return type;
    }

    public enum Type {
        CREATE_QUEUE, DELETE_QUEUE, PEEK_QUEUE, POP_QUEUE, SEND_TO,
        SEND_BROADCAST, POP_SENDER, PEEK_SENDER, QUERY_QUEUE
    }
}