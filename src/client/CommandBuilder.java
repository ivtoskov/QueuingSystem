package asl.client;

import asl.util.Command;

import java.util.Scanner;

/**
 * An interface used for building different types of commands
 * that can be sent to and executed by the middleware.
 */
public abstract class CommandBuilder {
    protected Command.Type type;

    public CommandBuilder() {}

    /**
     * Creates a command that can be sent and executed by the middleware.
     *
     * @param sc - Scanner object to read in the message content if necessary
     * @param sid - The ID of the sender of the message/initiatior of the command.
     * @param args - The arguments needed for this command.
     * @return A command object that may be sent to the middleware and executed there.
     */
    public abstract Command createCommand(Scanner sc, int sid, String args[]);
}