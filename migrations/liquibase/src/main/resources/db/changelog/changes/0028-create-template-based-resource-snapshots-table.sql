--liquibase formatted sql

--changeset orkg:28 dbms:postgresql

CREATE TABLE template_based_resource_snapshots
(
    id character varying NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_by uuid NOT NULL,
    model_version character varying NOT NULL,
    data jsonb NOT NULL,
    resource_id character varying NOT NULL,
    template_id character varying NOT NULL,
    handle character varying,
    PRIMARY KEY (id)
);

create index template_based_resource_snapshots_resource_id_uindex on template_based_resource_snapshots (resource_id);
create index template_based_resource_snapshots_template_id_uindex on template_based_resource_snapshots (template_id);
create unique index template_based_resource_snapshots_handle_uindex on template_based_resource_snapshots (handle);
