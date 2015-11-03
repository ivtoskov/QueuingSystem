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

SELECT generate_clients(120);
SELECT generate_queues(100);

CREATE OR REPLACE FUNCTION reload(senders INTEGER, repeats INTEGER, receivers INTEGER) RETURNS void AS $$
DECLARE
	msg VARCHAR(200);
BEGIN
	DELETE FROM Message;
	msg = 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx';
	FOR i IN 1..senders LOOP
		FOR j IN 1..repeats LOOP
			FOR k IN 1..receivers LOOP
				INSERT INTO Message(sid, rid, content, qid, atime)
				VALUES (i, k, msg, i, now());
			END LOOP;
		END LOOP;
	END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT reload(10, 1000, 20);
