/**
* Stored procedure for queue creation.
*
* @qid - The ID of the queue to be created.
*/
CREATE OR REPLACE FUNCTION create_queue (qid INTEGER)
    RETURNS void AS $$ 
BEGIN
    INSERT INTO Queue(id) VALUES (qid);
END;
$$ LANGUAGE plpgsql;

/**
* Stored procedure for queue deletion.
*
* @qid - The ID of the queue to be deleted.
*/
CREATE OR REPLACE FUNCTION delete_queue (qid INTEGER)
    RETURNS void AS $$ 
BEGIN
    DELETE FROM Queue WHERE id=qid;
END;
$$ LANGUAGE plpgsql;

/**
* Stored procedure for message sending.
*
* @nsid - The Client's ID of the sender
* @ncontent - The content of the message
* @nqid - The id of the queue where the message should be put.
* @ntime - The time of arrival of the message.
* @nrid - The id of the receiver. Can be left empty.
*/
CREATE OR REPLACE FUNCTION send_message(nsid INTEGER, ncontent TEXT, nqid INTEGER, ntime TIMESTAMP, nrid INTEGER DEFAULT NULL) RETURNS void AS $$
BEGIN
    INSERT INTO Message(sid, rid, content, qid, atime) VALUES (nsid, nrid, ncontent, nqid, ntime);
END;
$$ LANGUAGE plpgsql;

/**
* Stored procedure for peeking particular queue.
*
* @nrid - The ID of the receiver of the message.
* @nqid - The ID of the queue to be peeked.
*/
CREATE OR REPLACE FUNCTION peek_queue(nrid INTEGER, nqid INTEGER) RETURNS VARCHAR AS $$
DECLARE
	msg Message%ROWTYPE;
BEGIN
	SELECT * INTO msg FROM Message m WHERE (m.rid=nrid OR m.rid is NULL) AND (m.qid=nqid) ORDER BY m.atime LIMIT 1;
	return 'FROM: ' || msg.sid || ', CONTENT: "' || msg.content || '", ARRIVAL TIME: ' || msg.atime;
END;
$$ LANGUAGE plpgsql;

/**
* Stored procedure for popping particular queue.
*
* @nrid - The ID of the receiver of the message.
* @nqid - The ID of the queue to be popping.
*/
CREATE OR REPLACE FUNCTION pop_queue(nrid INTEGER, nqid INTEGER) RETURNS VARCHAR AS $$
DECLARE
	msg Message%ROWTYPE;
BEGIN
	SELECT * INTO msg FROM Message m WHERE (m.rid=nrid OR m.rid is NULL) AND (m.qid=nqid) ORDER BY m.atime LIMIT 1;
	DELETE FROM Message m where m.id=msg.id;
	return 'FROM: ' || msg.sid || ', CONTENT: "' || msg.content || '", ARRIVAL TIME: ' || msg.atime;
END;
$$ LANGUAGE plpgsql;

/**
* Stored procedure for querying messages from a particular sender. 
* The returned message is NOT being deleted.
*
* @nrid - The ID of the receiver of the message.
* @nsid - The ID of the sender of the message.
*/
CREATE OR REPLACE FUNCTION peek_sender(nrid INTEGER, nsid INTEGER) RETURNS VARCHAR AS $$
DECLARE
	msg Message%ROWTYPE;
BEGIN
	SELECT * INTO msg FROM Message m WHERE (m.rid=nrid OR m.rid is NULL) AND (m.sid=nsid) ORDER BY m.atime LIMIT 1;
	return 'FROM: ' || msg.sid || ', CONTENT: "' || msg.content || '", ARRIVAL TIME: ' || msg.atime;
END;
$$ LANGUAGE plpgsql;

/**
* Stored procedure for querying messages from a particular sender. 
* The returned message is being deleted.
*
* @nrid - The ID of the receiver of the message.
* @nsid - The ID of the sender of the message.
*/
CREATE OR REPLACE FUNCTION pop_sender(nrid INTEGER, nsid INTEGER) RETURNS VARCHAR AS $$
DECLARE
	msg Message%ROWTYPE;
BEGIN
	SELECT * INTO msg FROM Message m WHERE (m.rid=nrid OR m.rid is NULL) AND (m.sid=nsid) ORDER BY m.atime LIMIT 1;
	DELETE FROM Message m where m.id=msg.id;
	return 'FROM: ' || msg.sid || ', CONTENT: "' || msg.content || '", ARRIVAL TIME: ' || msg.atime;
END;
$$ LANGUAGE plpgsql;

/**
* Stored procedure returning all the queues
* that contain a message for a particular client.
*
* @nrid - The ID of the Client.
*/ 
CREATE OR REPLACE FUNCTION query_queue(nrid INTEGER) RETURNS VARCHAR AS $$
DECLARE
	cur CURSOR FOR SELECT DISTINCT q.id FROM Queue q JOIN Message m ON q.id=m.qid WHERE m.rid=nrid;
	response VARCHAR;
BEGIN
	response = 'Queues with messages for you:';
	FOR t in cur LOOP
		response = response || ' ' || t.id;
	END LOOP;
	return response;
END;
$$ LANGUAGE plpgsql;
