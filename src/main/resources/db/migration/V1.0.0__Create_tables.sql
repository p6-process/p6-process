CREATE TABLE REQUEST_PROCESS_QUEUE (
     id SERIAL PRIMARY KEY,
     date timestamp DEFAULT now(),
     count bigint DEFAULT 0,
     data jsonb
);

-- create pub-sub process function
CREATE OR REPLACE FUNCTION process_msg_pub() RETURNS trigger AS
$$
BEGIN
    PERFORM pg_notify('request_process_queue', NEW.id::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- create pub-sub process trigger
CREATE TRIGGER request_process_queue_trigger AFTER INSERT ON REQUEST_PROCESS_QUEUE FOR EACH ROW EXECUTE PROCEDURE process_msg_pub();