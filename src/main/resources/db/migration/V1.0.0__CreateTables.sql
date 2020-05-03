create table PROCESS_MSG
(
    id             SERIAL,
    created        bigint,
    ref            varchar(255),
    primary key (id)
);

create table PROCESS_INSTANCE
(
    id             varchar(255) not null,
    data           jsonb,
    messageId      varchar(255),
    parent         varchar(255),
    processId      varchar(255),
    processVersion varchar(255),
    status         varchar(255),
    OPTLOCK        int4,
    primary key (id)
);

create table PROCESS_TOKEN
(
    id              varchar(255) not null,
    data            jsonb,
    executionId     varchar(255),
    messageId       varchar(255),
    nodeName        varchar(255),
    parent          varchar(255),
    processId       varchar(255),
    processInstance varchar(255),
    processVersion  varchar(255),
    reference       varchar(255),
    status          varchar(255),
    type            varchar(255),
    OPTLOCK         int4,
    primary key (id)
);
