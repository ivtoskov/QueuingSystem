CREATE SEQUENCE seq_msg;

CREATE TABLE Client (
	ID INTEGER PRIMARY KEY
);

CREATE TABLE Queue (
	ID INTEGER PRIMARY KEY 
);

CREATE TABLE Message (
	ID INTEGER PRIMARY KEY DEFAULT nextval('seq_msg'),
	SID INTEGER REFERENCES Client(id),
	RID INTEGER REFERENCES Client(id),
	Content TEXT,
	QID INTEGER REFERENCES Queue(id) NOT NULL,
	atime TIMESTAMP
);

CREATE INDEX queue_index ON message(qid, rid, atime);
CREATE INDEX sender_index ON message(sid, rid, atime);