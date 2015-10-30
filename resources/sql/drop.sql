DROP FUNCTION IF EXISTS peek_queue(integer, integer);
DROP FUNCTION IF EXISTS create_queue(integer);
DROP FUNCTION IF EXISTS delete_queue(integer);
DROP FUNCTION IF EXISTS generate_queues(integer);
DROP FUNCTION IF EXISTS generate_clients(integer);
DROP FUNCTION IF EXISTS peek_sender(integer, integer);
DROP FUNCTION IF EXISTS pop_queue(integer, integer);
DROP FUNCTION IF EXISTS pop_sender(integer, integer);
DROP FUNCTION IF EXISTS query_queue(integer);
DROP FUNCTION IF EXISTS send_message(integer, text, integer, timestamp without time zone, integer);

DROP TABLE IF EXISTS Message;
DROP TABLE IF EXISTS Client;
DROP TABLE IF EXISTS Queue;

DROP SEQUENCE seq_msg;
