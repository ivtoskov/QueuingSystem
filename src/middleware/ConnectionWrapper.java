package asl.middleware;

import org.apache.log4j.Logger;

import java.lang.String;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.List;

import asl.util.Command;
import asl.util.Message;

/**
 * A class that wraps a connection together with its prepared statements
 * and defines methods that serve as an interface to the database
 * for the different supported operations.
 */
public class ConnectionWrapper {
    private static final String FAILED_TO_POP_SENDER = "FAILED to pop sender.";
    private static final String FAILED_TO_PEEK_SENDER = "FAILED to peek sender.";
    private static final String FAILED_TO_QUERY_QUEUES = "FAILED to query queues.";
    private static final String FAILED_TO_SEND_MESSAGE = "FAILED to send message.";
    private static final String SUCCESSFULLY_SENT_MESSAGE = "SUCCESSFULLY sent message";
    private static final String FAILED_TO_PROCESS_COMMAND = "FAILED to process command.";
    private static final String FAILED_TO_POP_QUEUE = "FAILED to pop queue.";
    private static final String FAILED_TO_PEEK_QUEUE = "FAILED to peek queue.";
    private static final String FAILED_TO_CREATE_QUEUE = "FAILED to create queue ";
    private static final String SUCCESSFULLY_CREATED_QUEUE = "SUCCESSFULLY created queue ";
    private static final String FAILED_TO_DELETE_QUEUE = "FAILED to delete queue ";
    private static final String SUCCESSFULLY_DELETED_QUEUE = "SUCCESSFULLY deleted queue ";
    public static final String ERROR_CLOSING_SQL_CONNECTION = "Error while closing SQL connection.";
    public static final String ERROR_PREPARING_CONNECTION = "Error while preparing the database connection.";
    public static final String NO_MESSAGES_ARE_WAITING = "No messages are waiting";
    private static Logger logger = Logger.getLogger(ConnectionWrapper.class);
    private static final String CREATE_QUEUE_STRING = " { call create_queue( ? ) }";
    private static final String DELETE_QUEUE_STRING = " { call delete_queue( ? ) }";
    private static final String PEEK_QUEUE_STRING = "{ ? = call peek_queue( ?, ? ) }";
    private static final String POP_QUEUE_STRING = "{ ? = call pop_queue( ?, ? ) }";
    private static final String SEND_TO_STRING = "{ call send_message(?, ?, ?, ?, ?) }";
    private static final String SEND_BROADCAST_STRING = "{ call send_message(?, ?, ?, ?) }";
    private static final String QUERY_QUEUE_STRING = "{ ? = call query_queue( ? ) }";
    private static final String PEEK_SENDER_STRING = "{ ? = call peek_sender( ?, ? ) }";
    private static final String POP_SENDER_STRING = "{ ? = call pop_sender( ?, ? ) }";
    private boolean corrupted;
    private Connection connection;
    private CallableStatement createQueue;
    private CallableStatement deleteQueue;
    private CallableStatement peekQueue;
    private CallableStatement popQueue;
    private CallableStatement sendTo;
    private CallableStatement sendBroadcast;
    private CallableStatement queryQueue;
    private CallableStatement peekSender;
    private CallableStatement popSender;

    public ConnectionWrapper(Connection connection) {
        try {
            this.connection = connection;
            this.createQueue = connection.prepareCall(CREATE_QUEUE_STRING);
            this.deleteQueue = connection.prepareCall(DELETE_QUEUE_STRING);
            this.peekQueue = connection.prepareCall(PEEK_QUEUE_STRING);
            this.popQueue = connection.prepareCall(POP_QUEUE_STRING);
            this.sendTo = connection.prepareCall(SEND_TO_STRING);
            this.sendBroadcast = connection.prepareCall(SEND_BROADCAST_STRING);
            this.queryQueue = connection.prepareCall(QUERY_QUEUE_STRING);
            this.peekSender = connection.prepareCall(PEEK_SENDER_STRING);
            this.popSender = connection.prepareCall(POP_SENDER_STRING);
            corrupted = false;
        } catch (SQLException e) {
            logger.error(ERROR_PREPARING_CONNECTION);
            corrupted = true;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
            if (createQueue != null && !createQueue.isClosed()) createQueue.close();
            if (deleteQueue != null && !deleteQueue.isClosed()) createQueue.close();
            if (peekQueue != null && !peekQueue.isClosed()) peekQueue.close();
            if (popQueue != null && !popQueue.isClosed()) popQueue.close();
            if (sendTo != null && !sendTo.isClosed()) sendTo.close();
            if (sendBroadcast != null && !sendBroadcast.isClosed()) sendBroadcast.close();
            if (queryQueue != null && !queryQueue.isClosed()) queryQueue.close();
            if (peekSender != null && !peekSender.isClosed()) peekSender.close();
            if (popSender != null && !popSender.isClosed()) popSender.close();
        } catch (SQLException e) {
            logger.error(ERROR_CLOSING_SQL_CONNECTION);
        }
    }

    public String handle(Command command) {
        switch (command.getType()) {
            case CREATE_QUEUE:
                return createQueue(command);
            case DELETE_QUEUE:
                return deleteQueue(command);
            case PEEK_QUEUE:
                return peekQueue(command);
            case POP_QUEUE:
                return popQueue(command);
            case SEND_TO:
                return sendTo(command);
            case SEND_BROADCAST:
                return sendBroadcast(command);
            case QUERY_QUEUE:
                return queryQueue(command);
            case PEEK_SENDER:
                return peekSender(command);
            case POP_SENDER:
                return popSender(command);
            default:
                return FAILED_TO_PROCESS_COMMAND;
        }

    }

    private String createQueue(Command command) {
        try {
            createQueue.setInt(1, command.getMsg().getQid());
            createQueue.execute();
            return SUCCESSFULLY_CREATED_QUEUE + command.getMsg().getQid();
        } catch (SQLException e) {
            logger.error(FAILED_TO_CREATE_QUEUE);
            return FAILED_TO_CREATE_QUEUE + command.getMsg().getQid();
        }
    }

    private String deleteQueue(Command command) {
        try {
            deleteQueue.setInt(1, command.getMsg().getQid());
            deleteQueue.execute();
            return SUCCESSFULLY_DELETED_QUEUE + command.getMsg().getQid();
        } catch (SQLException e) {
            logger.info(FAILED_TO_DELETE_QUEUE);
            return FAILED_TO_DELETE_QUEUE + command.getMsg().getQid();
        }
    }

    private String peekQueue(Command command) {
        try {
            peekQueue.registerOutParameter(1, Types.VARCHAR);
            peekQueue.setInt(2, command.getMsg().getRid());
            peekQueue.setInt(3, command.getMsg().getQid());
            peekQueue.execute();
            String response = peekQueue.getString(1);
            return (response==null) ? NO_MESSAGES_ARE_WAITING : response;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(FAILED_TO_PEEK_QUEUE);
        }

        return FAILED_TO_PEEK_QUEUE;
    }

    private String popQueue(Command command) {
        try {
            popQueue.registerOutParameter(1, Types.VARCHAR);
            popQueue.setInt(2, command.getMsg().getRid());
            popQueue.setInt(3, command.getMsg().getQid());
            popQueue.execute();
            String response = popQueue.getString(1);
            return (response==null) ? NO_MESSAGES_ARE_WAITING : response;
        } catch (SQLException e) {
            logger.error(FAILED_TO_POP_QUEUE);
        }

        return FAILED_TO_POP_QUEUE;
    }

    private String sendTo(Command command) {
        try {
            sendTo.setInt(1, command.getMsg().getSid());
            sendTo.setString(2, command.getMsg().getContent());
            sendTo.setInt(3, command.getMsg().getQid());
            sendTo.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));
            sendTo.setInt(5, command.getMsg().getRid());
            sendTo.execute();
            return SUCCESSFULLY_SENT_MESSAGE;
        } catch (SQLException e) {
            logger.error(FAILED_TO_SEND_MESSAGE);
        }

        return FAILED_TO_SEND_MESSAGE;
    }

    private String sendBroadcast(Command command) {
        try {
            sendBroadcast.setInt(1, command.getMsg().getSid());
            sendBroadcast.setString(2, command.getMsg().getContent());
            sendBroadcast.setInt(3, command.getMsg().getQid());
            sendBroadcast.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));
            sendBroadcast.execute();
            return SUCCESSFULLY_SENT_MESSAGE;
        } catch (SQLException e) {
            logger.error(FAILED_TO_SEND_MESSAGE);
        }

        return FAILED_TO_SEND_MESSAGE;
    }

    private String queryQueue(Command command) {
        try {
            queryQueue.registerOutParameter(1, Types.VARCHAR);
            queryQueue.setInt(2, command.getMsg().getRid());
            queryQueue.execute();
            String response = queryQueue.getString(1);
            return response;
        } catch (SQLException e) {
            logger.error(FAILED_TO_QUERY_QUEUES);
        }

        return FAILED_TO_QUERY_QUEUES;
    }

    private String peekSender(Command command) {
        try {
            peekSender.registerOutParameter(1, Types.VARCHAR);
            peekSender.setInt(2, command.getMsg().getRid());
            peekSender.setInt(3, command.getMsg().getSid());
            peekSender.execute();
            String response = peekSender.getString(1);
            return (response==null) ? NO_MESSAGES_ARE_WAITING : response;
        } catch (SQLException e) {
            logger.error(FAILED_TO_PEEK_SENDER);
        }

        return FAILED_TO_PEEK_SENDER;
    }

    private String popSender(Command command) {
        try {
            popSender.registerOutParameter(1, Types.VARCHAR);
            popSender.setInt(2, command.getMsg().getRid());
            popSender.setInt(3, command.getMsg().getSid());
            popSender.execute();
            String response = popSender.getString(1);
            return (response==null) ? NO_MESSAGES_ARE_WAITING : response;
        } catch (SQLException e) {
            logger.error(FAILED_TO_POP_SENDER);
        }

        return FAILED_TO_POP_SENDER;
    }

    public boolean isCorrupted() {
        return corrupted;
    }
}