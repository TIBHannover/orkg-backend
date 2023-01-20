--liquibase formatted sql

--changeset orkg:15 dbms:postgresql

create table images
(
    id uuid,
	data bytea not null,
    mime_type character varying not null
        constraint mime_type_is_lower_case check (mime_type = lower(mime_type)),
    created_by uuid not null
        constraint uploaded_by_fk references users,
    created_at timestamptz not null,
    primary key (id)
);
