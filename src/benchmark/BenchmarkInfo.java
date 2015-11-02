package asl.benchmark;

public class BenchmarkInfo {
    private int[] senders;
    private int[] receivers;
    private int[] queues;
    private int duration;
    private int operationType;
    private int messageLength;
    private boolean broadcast;
    private boolean crossSend;
    private boolean crossQueue;

    public BenchmarkInfo() {
        this.duration = 0;
        this.operationType = 0;
        this.messageLength = 0;
        this.broadcast = false;
        this.crossQueue = false;
        this.crossSend = false;
        this.senders = null;
        this.receivers = null;
        this.queues = null;
    }

    public void reset() {
        this.operationType = 0;
        this.messageLength = 0;
        this.broadcast = false;
        this.crossQueue = false;
        this.crossSend = false;
        this.senders = null;
        this.receivers = null;
        this.queues = null;
    }

    public int[] getSenders() {
        return senders;
    }

    public void setSenders(int[] senders) {
        this.senders = senders;
    }

    public int[] getReceivers() {
        return receivers;
    }

    public void setReceivers(int[] receivers) {
        this.receivers = receivers;
    }

    public int[] getQueues() {
        return queues;
    }

    public void setQueues(int[] queues) {
        this.queues = queues;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public boolean isCrossSend() {
        return crossSend;
    }

    public void setCrossSend(boolean crossSend) {
        this.crossSend = crossSend;
    }

    public boolean isCrossQueue() {
        return crossQueue;
    }

    public void setCrossQueue(boolean crossQueue) {
        this.crossQueue = crossQueue;
    }
}