--liquibase formatted sql

--changeset orkg:35 dbms:postgresql

CREATE TABLE comparison_tables
(
    comparison_id character varying NOT NULL,
    selected_paths jsonb NOT NULL,
    titles jsonb NOT NULL,
    subtitles jsonb NOT NULL,
    values jsonb NOT NULL,
    PRIMARY KEY (comparison_id)
);
