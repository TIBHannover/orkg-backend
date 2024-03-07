--liquibase formatted sql

--changeset orkg:19 dbms:postgresql

create table public.observatory_filters
(
    id uuid not null,
    observatory_id uuid not null
        constraint observatory_id_fk references observatories,
    label character varying not null,
    created_by uuid not null
        constraint created_by_fk references users,
    created_at timestamptz not null,
    path character varying not null,
    range character varying not null,
    exact boolean not null,
    featured boolean not null,
    primary key (id)
);
