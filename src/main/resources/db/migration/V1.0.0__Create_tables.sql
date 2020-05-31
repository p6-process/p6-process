CREATE TABLE PROCESS_MSG (
    id SERIAL PRIMARY KEY,
    created timestamp DEFAULT now(),
    ref varchar(255),
    cmd varchar(255)
);

CREATE TABLE TOKEN_MSG (
    id SERIAL PRIMARY KEY,
    created timestamp DEFAULT now(),
    ref varchar(255),
    cmd varchar(255)
);

CREATE TABLE SINGLETON_MSG (
   id SERIAL PRIMARY KEY,
   created timestamp DEFAULT now(),
   ref varchar(255),
   cmd varchar(255)
);

CREATE TABLE PROCESS_INSTANCE (
    id varchar(255) NOT NULL PRIMARY KEY,
    data jsonb,
    parent varchar(255),
    processId varchar(255),
    processVersion varchar(255),
    status varchar(255)
);

CREATE TABLE PROCESS_TOKEN (
    id varchar(255) NOT NULL PRIMARY KEY,
    data jsonb,
    executionId varchar(255),
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

-- create pub-sub process function
CREATE OR REPLACE FUNCTION process_msg_pub() RETURNS trigger AS
$$
BEGIN
  PERFORM pg_notify('process_msg', NEW.id::text);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- create pub-sub process trigger
CREATE TRIGGER process_msg_trigger AFTER INSERT ON PROCESS_MSG FOR EACH ROW EXECUTE PROCEDURE process_msg_pub();

-- create pub-sub token function
CREATE OR REPLACE FUNCTION token_msg_pub() RETURNS trigger AS
$$
BEGIN
    PERFORM pg_notify('token_msg', NEW.id::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- create pub-sub token trigger
CREATE TRIGGER token_msg_trigger AFTER INSERT ON TOKEN_MSG FOR EACH ROW EXECUTE PROCEDURE token_msg_pub();

-- create pub-sub singleton function
CREATE OR REPLACE FUNCTION singleton_msg_pub() RETURNS trigger AS
$$
BEGIN
    PERFORM pg_notify('singleton_msg', NEW.id::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- create pub-sub singleton trigger
CREATE TRIGGER singleton_msg_trigger AFTER INSERT ON SINGLETON_MSG FOR EACH ROW EXECUTE PROCEDURE singleton_msg_pub();