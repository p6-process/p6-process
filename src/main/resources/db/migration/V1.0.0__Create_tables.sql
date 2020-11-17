CREATE TABLE PROCESS_INSTANCE (
   id varchar(255) NOT NULL PRIMARY KEY,
   data jsonb,
   cmdId varchar(255),
   parent varchar(255),
   processId varchar(255),
   processVersion varchar(255),
   status varchar(255)
);

CREATE TABLE PROCESS_TOKEN (
   id varchar(255) NOT NULL PRIMARY KEY,
   data jsonb,
   nodeName varchar(255),
   parent varchar(255),
   processId varchar(255),
   processInstance varchar(255),
   processVersion varchar(255),
   reference varchar(255),
   status varchar(255),
   type varchar(255),
   createdFrom varchar(255)[]
);

------------------------------------------------------
-- REQUEST_PROCESS_QUEUE
CREATE TABLE REQUEST_PROCESS_QUEUE (
     id SERIAL PRIMARY KEY,
     date timestamp DEFAULT now(),
     modification timestamp DEFAULT now(),
     count bigint DEFAULT 0,
     label varchar,
     data jsonb,
     header jsonb
);
CREATE OR REPLACE FUNCTION request_process_queue_pub() RETURNS trigger AS
$$
BEGIN
    PERFORM pg_notify('REQUEST_PROCESS_QUEUE', NEW.id::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER request_process_queue_trigger AFTER INSERT ON REQUEST_PROCESS_QUEUE FOR EACH ROW EXECUTE PROCEDURE request_process_queue_pub();

-- RESPONSE_PROCESS_QUEUE
CREATE TABLE RESPONSE_PROCESS_QUEUE (
   id SERIAL PRIMARY KEY,
   date timestamp DEFAULT now(),
   modification timestamp DEFAULT now(),
   count bigint DEFAULT 0,
   label varchar,
   data jsonb,
   header jsonb
);
CREATE OR REPLACE FUNCTION response_process_queue_pub() RETURNS trigger AS
$$
BEGIN
    PERFORM pg_notify('RESPONSE_PROCESS_QUEUE', NEW.id::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER response_process_queue_trigger AFTER INSERT ON REQUEST_PROCESS_QUEUE FOR EACH ROW EXECUTE PROCEDURE response_process_queue_pub();

-- TOKEN_EXECUTE_QUEUE
CREATE TABLE TOKEN_EXECUTE_QUEUE (
    id SERIAL PRIMARY KEY,
    date timestamp DEFAULT now(),
    modification timestamp DEFAULT now(),
    count bigint DEFAULT 0,
    label varchar,
    data jsonb,
    header jsonb
);
CREATE OR REPLACE FUNCTION token_execute_queue_pub() RETURNS trigger AS
$$
BEGIN
    PERFORM pg_notify('TOKEN_EXECUTE_QUEUE', NEW.id::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER token_execute_queue_trigger AFTER INSERT ON TOKEN_EXECUTE_QUEUE FOR EACH ROW EXECUTE PROCEDURE token_execute_queue_pub();