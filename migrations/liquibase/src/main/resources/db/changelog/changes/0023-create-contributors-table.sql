--liquibase formatted sql

--changeset orkg:23 dbms:postgresql

create table contributors
(
    id              uuid                      not null
        constraint contributors_pk
            primary key,
    display_name    varchar                   not null,
    joined_at       timestamptz default now() not null,
    organization_id uuid,
    observatory_id  uuid,
    email_md5       varchar(32),
    curator         boolean     default false not null,
    admin           boolean     default false not null
);

create unique index contributors_id_uindex on contributors (id);

--rollback drop table "contributors"
