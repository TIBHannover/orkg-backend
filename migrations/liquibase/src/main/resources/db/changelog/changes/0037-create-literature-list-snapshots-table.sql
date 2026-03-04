--liquibase formatted sql

--changeset orkg:37 dbms:postgresql

CREATE TABLE literature_list_snapshots
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

create unique index literature_list_snapshots_resource_id_uindex on literature_list_snapshots (resource_id);
create index literature_list_snapshots_template_id_index on literature_list_snapshots (root_id);
