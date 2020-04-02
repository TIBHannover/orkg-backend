--liquibase formatted sql

--changeset mprinz:1 dbms:postgresql

create table users
(
    id       uuid                      not null
        constraint user_pk primary key,
    email    varchar                   not null,
    password varchar                   not null,
    enabled  boolean     default false not null,
    created  timestamptz default now() not null
);

create unique index users_id_uindex on users (id);
create unique index users_email_uindex on users (email);
create index users_enabled_index on users (enabled);


create table roles
(
    id   uuid    not null
        constraint roles_users_id_fk references users,
    name varchar not null
);

create unique index roles_id_name_uindex on roles (id, name);

--rollback drop table "users"; drop table "roles";
