--liquibase formatted sql

--changeset mprinz:2 dbms:postgresql

alter table users
    add display_name varchar not null default 'an anonymous user';

--rollback alter table users drop display_name;
