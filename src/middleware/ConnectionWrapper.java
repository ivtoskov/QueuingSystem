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

public class ConnectionWrapper {
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
            logger.error("Error while preparing the database connection.");
            corrupted = true;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error while closing SQL connection.");
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
                return "Failed to process command.";
        }

    }

    private String createQueue(Command command) {
        try {
            createQueue.setInt(1, command.getMsg().getQid());
            createQueue.execute();
            return "SUCCESSFULLY created queue " + command.getMsg().getQid();
        } catch (SQLException e) {
            logger.info("Error while trying to create a queue");
            return "FAILED to create queue " + command.getMsg().getQid();
        }
    }

    private String deleteQueue(Command command) {
        try {
            deleteQueue.setInt(1, command.getMsg().getQid());
            deleteQueue.execute();
            return "SUCCESSFULLY deleted queue " + command.getMsg().getQid();
        } catch (SQLException e) {
            logger.info("Error while trying to delete a queue");
            return "FAILED to delete queue " + command.getMsg().getQid();
        }
    }

    private String peekQueue(Command command) {
        try {
            peekQueue.registerOutParameter(1, Types.VARCHAR);
            peekQueue.setInt(2, command.getMsg().getRid());
            peekQueue.setInt(3, command.getMsg().getQid());
            peekQueue.execute();
            String response = peekQueue.getString(1);
            return response;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Failed to peek queue.");
        }

        return "FAILED to peek queue.";
    }

    private String popQueue(Command command) {
        try {
            popQueue.registerOutParameter(1, Types.VARCHAR);
            popQueue.setInt(2, command.getMsg().getRid());
            popQueue.setInt(3, command.getMsg().getQid());
            popQueue.execute();
            String response = popQueue.getString(1);
            return response;
        } catch (SQLException e) {
            logger.error("Failed to pop queue.");
        }

        return "FAILED to pop queue.";
    }

    private String sendTo(Command command) {
        try {
            sendTo.setInt(1, command.getMsg().getSid());
            sendTo.setString(2, command.getMsg().getContent());
            sendTo.setInt(3, command.getMsg().getQid());
            sendTo.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));
            sendTo.setInt(5, command.getMsg().getRid());
            sendTo.execute();
            return "SUCCESSFULLY sent message";
        } catch (SQLException e) {
            logger.error("FAILED to send message.");
        }

        return "FAILED to send message.";
    }

    private String sendBroadcast(Command command) {
        try {
            sendBroadcast.setInt(1, command.getMsg().getSid());
            sendBroadcast.setString(2, command.getMsg().getContent());
            sendBroadcast.setInt(3, command.getMsg().getQid());
            sendBroadcast.setTimestamp(4, new Timestamp((new java.util.Date()).getTime()));
            sendBroadcast.execute();
            return "SUCCESSFULLY sent message";
        } catch (SQLException e) {
            logger.error("FAILED to send message.");
        }

        return "FAILED to send message.";
    }

    private String queryQueue(Command command) {
        try {
            queryQueue.registerOutParameter(1, Types.VARCHAR);
            queryQueue.setInt(2, command.getMsg().getRid());
            queryQueue.execute();
            String response = queryQueue.getString(1);
            return response;
        } catch (SQLException e) {
            logger.error("Failed to query queues.");
        }

        return "FAILED to query queues.";
    }

    private String peekSender(Command command) {
        try {
            peekSender.registerOutParameter(1, Types.VARCHAR);
            peekSender.setInt(2, command.getMsg().getRid());
            peekSender.setInt(3, command.getMsg().getSid());
            peekSender.execute();
            String response = peekSender.getString(1);
            return response;
        } catch (SQLException e) {
            logger.error("Failed to peek sender.");
        }

        return "FAILED to peek sender.";
    }

    private String popSender(Command command) {
        try {
            popSender.registerOutParameter(1, Types.VARCHAR);
            popSender.setInt(2, command.getMsg().getRid());
            popSender.setInt(3, command.getMsg().getSid());
            popSender.execute();
            String response = popSender.getString(1);
            return response;
        } catch (SQLException e) {
            logger.error("Failed to pop sender.");
        }

        return "FAILED to pop sender.";
    }

    public boolean isCorrupted() {
        return corrupted;
    }
}