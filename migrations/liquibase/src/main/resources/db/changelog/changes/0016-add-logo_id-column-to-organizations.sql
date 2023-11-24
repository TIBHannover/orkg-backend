--liquibase formatted sql

--changeset orkg:16 dbms:postgresql

--add logo_id column to organizations
alter table organizations
    add column logo_id uuid
        constraint logo_id_fk references images;
