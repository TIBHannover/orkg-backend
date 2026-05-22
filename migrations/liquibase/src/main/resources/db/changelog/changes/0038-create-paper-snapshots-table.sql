--liquibase formatted sql

--changeset orkg:38 dbms:postgresql

CREATE TABLE paper_snapshots
(
    id character varying NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_at_offset_total_seconds integer NOT NULL,
    created_by uuid NOT NULL,
    model_version character varying NOT NULL,
    data jsonb NOT NULL,
    resource_id character varying NOT NULL,
    PRIMARY KEY (id)
);

create unique index paper_snapshots_resource_id_uindex on paper_snapshots (resource_id);
