CREATE OR REPLACE FUNCTION generate_clients(num INTEGER) RETURNS void AS $$
BEGIN
	FOR i IN 1..num LOOP
		INSERT INTO CLIENT(ID) VALUES(i);
	END LOOP;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_queues(num INTEGER) RETURNS void AS $$
BEGIN
	FOR i IN 1..num LOOP
		INSERT INTO Queue(ID) VALUES(i);
	END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT generate_clients(50);
SELECT generate_queues(10);

INSERT INTO Message(sid, rid, content, qid, atime) VALUES 
	(1, 2, 'Hello, how are you?', 1, '2015-10-15 22:49:02.235541'),
	(2, 1, 'I am fine, thank you. How are you?', 1, '2015-10-15 22:50:02.235541'),
	(1, 2, 'I am also fine, thanks!', 1, '2015-10-15 22:51:02.235541'),
	(3, 4, 'These guys are boring.', 2, '2015-10-15 22:52:02.235541'),
	(4, 3, 'I agree, their conversation does not make any sense', 2, '2015-10-15 22:53:02.235541'),
	(1, 3, 'Hey, what is your problem?',3, '2015-10-15 22:54:02.235541');
