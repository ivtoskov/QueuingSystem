package asl.util;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * JavaBean class that corresponds
 * to a message in the system.
 */
public class Message implements Serializable {
    private final String content;
    private final int sid;
    private final int rid;
    private final int qid;
    private Timestamp time;
    private int id;

    public Message(int id, String content, int sid, int rid, int qid, Timestamp time) {
        this.id = id;
        this.content = content;
        this.sid = sid;
        this.rid = rid;
        this.qid = qid;
        this.time = time;
    }

    public Message(String content, int sid, int rid, int qid) {
        this(-1, content, sid, rid, qid, null);
    }

    public Message(String content, int sid, int qid) {
        this(content, sid, -1, qid);
    }

    public String getContent() {
        return content;
    }

    public int getSid() {
        return sid;
    }

    public int getRid() {
        return rid;
    }

    public int getQid() {
        return qid;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}