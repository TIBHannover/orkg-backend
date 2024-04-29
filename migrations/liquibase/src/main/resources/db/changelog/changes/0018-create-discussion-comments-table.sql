--liquibase formatted sql

--changeset orkg:18 dbms:postgresql

create table public.discussion_comments
(
    id uuid not null,
    topic character varying not null,
    message character varying not null,
    created_by uuid not null
        constraint created_by_fk references users,
    created_at timestamptz not null,
    primary key (id)
);
