--liquibase formatted sql

--changeset orkg:12 dbms:postgresql

--adding column type in organization to handle different types of organizations
alter table organizations
    add type varchar;

--replacing default value with name of observatories
update organizations set type='GENERAL';

create table conferences_metadata
(
    organization_id uuid not null
        constraint organizations_id_fk references organizations,
    date DATE not null,
    is_double_blind bool not null
);
