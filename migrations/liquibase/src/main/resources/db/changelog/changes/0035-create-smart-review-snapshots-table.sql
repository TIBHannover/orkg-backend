--liquibase formatted sql

--changeset orkg:35 dbms:postgresql

CREATE TABLE smart_review_snapshots
(
    id character varying NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_at_offset_total_seconds integer NOT NULL,
    created_by uuid NOT NULL,
    model_version character varying NOT NULL,
    data jsonb NOT NULL,
    resource_id character varying NOT NULL,
    root_id character varying NOT NULL,
    PRIMARY KEY (id)
);

create unique index smart_review_snapshots_resource_id_uindex on smart_review_snapshots (resource_id);
create index smart_review_snapshots_template_id_index on smart_review_snapshots (root_id);
