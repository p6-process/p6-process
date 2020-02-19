
create table CREATED_FROM (
                              TOKEN_GUID varchar(255) not null,
                              createdFrom varchar(255)
);

create table PROCESS_DEF (
                             C_GUID varchar(255) not null,
                             C_OPLOCK int4,
                             PROCESS_APP varchar(255),
                             PROCESS_MODULE varchar(255),
                             PROCESS_ID varchar(255),
                             PROCESS_URL varchar(255),
                             PROCESS_VERSION varchar(255),
                             PROCESS_RESOURCE varchar(255),
                             primary key (C_GUID)
);

create table PROCESS_DEF_CONTENT (
                                     C_GUID varchar(255) not null,
                                     C_OPLOCK int4,
                                     DATA bytea,
                                     primary key (C_GUID)
);

create table PROCESS_DEPLOYMENT (
                                    C_GUID varchar(255) not null,
                                    C_OPLOCK int4,
                                    PROCESS_DEF_GUID varchar(255),
                                    PROCESS_ID varchar(255),
                                    PROCESS_VERSION varchar(255),
                                    primary key (C_GUID)
);

create table PROCESS_INSTANCE (
                                  C_GUID varchar(255) not null,
                                  C_OPLOCK int4,
                                  PROCESS_DEF_GUID varchar(255),
                                  PROCESS_ID varchar(255),
                                  PROCESS_PARENT_GUID varchar(255),
                                  PROCESS_VERSION varchar(255),
                                  STATUS varchar(255),
                                  primary key (C_GUID)
);

create table PROCESS_INSTANCE_CONTENT (
                                          C_GUID varchar(255) not null,
                                          C_OPLOCK int4,
                                          DATA bytea,
                                          primary key (C_GUID)
);

create table PROCESS_TOKEN (
                               C_GUID varchar(255) not null,
                               C_OPLOCK int4,
                               CREATE_NODE_NAME varchar(255),
                               FINISHED_DATE timestamp,
                               NODE_NAME varchar(255),
                               PARENT_TOKEN_GUID varchar(255),
                               NODE_PREVIOUS varchar(255),
                               PROCESS_ID varchar(255),
                               PROCESS_INSTANCE_GUID varchar(255),
                               PROCESS_VERSION varchar(255),
                               REF_TOKEN_GUID varchar(255),
                               START_NODE_NAME varchar(255),
                               TOKEN_STATUS varchar(255),
                               TOKEN_TYPE varchar(255),
                               MESSAGE_ID varchar(255),
                               primary key (C_GUID)
);

create table PROCESS_TOKEN_CONTENT (
                                       C_GUID varchar(255) not null,
                                       C_OPLOCK int4,
                                       DATA bytea,
                                       primary key (C_GUID)
);

alter table PROCESS_DEF
    add constraint PROCESS_DEF_IDX unique (PROCESS_ID, PROCESS_VERSION);
create index PD_IDX_PROCESS_ID on PROCESS_DEPLOYMENT (PROCESS_ID);

alter table PROCESS_TOKEN
    add constraint TOKEN_UC_1 unique (PROCESS_INSTANCE_GUID, START_NODE_NAME);

alter table PROCESS_TOKEN
    add constraint TOKEN_UC_REF unique (PROCESS_INSTANCE_GUID, REF_TOKEN_GUID, CREATE_NODE_NAME);

alter table PROCESS_TOKEN
    add constraint TOKEN_UC_CHILD unique (PROCESS_INSTANCE_GUID, PARENT_TOKEN_GUID, CREATE_NODE_NAME);

alter table CREATED_FROM
    add constraint FK_TOKEN_GUID
        foreign key (TOKEN_GUID)
            references PROCESS_TOKEN;
