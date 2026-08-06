--liquibase formatted sql

--changeset orkg:41 dbms:postgresql

create sequence "aggregate-event-global-index-sequence"
    increment by 1;

create table aggregate_event_entry
(
    global_index              bigint       not null
        primary key,
    aggregate_sequence_number bigint,
    aggregate_identifier      varchar(255),
    identifier                varchar(255) not null,
    version                   varchar(255) not null,
    type                      varchar(255) not null,
    timestamp                 varchar(255) not null,
    aggregate_type            varchar(255),
    metadata                  BYTEA,
    payload                   BYTEA        not null,
    unique (aggregate_identifier, aggregate_sequence_number)
);

create table snapshot_event_entry
(
    sequence_number      bigint       not null,
    aggregate_identifier varchar(255) not null,
    event_identifier     varchar(255) not null
        unique,
    payload_revision     varchar(255),
    payload_type         varchar(255) not null,
    time_stamp           varchar(255) not null,
    type                 varchar(255) not null,
    meta_data            oid,
    payload              oid          not null,
    primary key (sequence_number, aggregate_identifier, type)
);

create sequence association_value_entry_seq
    increment by 1;

create table event_publication
(
    completion_date  timestamp(6) with time zone,
    publication_date timestamp(6) with time zone,
    id               uuid not null
        primary key,
    event_type       varchar(255),
    listener_id      varchar(255),
    serialized_event varchar(255)
);

CREATE TABLE token_entry
(
    processor_name VARCHAR(512) NOT NULL,
    segment        INTEGER      NOT NULL,
    owner          VARCHAR(512),
    timestamp      VARCHAR(512) NOT NULL,
    token          BYTEA,
    token_type     VARCHAR(512),
    mask           INTEGER      NOT NULL,
    CONSTRAINT token_entry_pkey PRIMARY KEY (processor_name, segment)
);
