--liquibase formatted sql

--changeset orkg:32 dbms:postgresql

CREATE TABLE csvs
(
    id uuid NOT NULL,
    name character varying NOT NULL,
    data character varying NOT NULL,
    data_md5 character varying NOT NULL,
    type character varying NOT NULL,
    format character varying NOT NULL,
    state character varying NOT NULL,
    validation_job_id character varying,
    import_job_id character varying,
    created_by uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_at_offset_total_seconds integer NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

create unique index csvs_data_md5_uindex on csvs (data_md5);
