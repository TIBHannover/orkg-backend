--liquibase formatted sql

--changeset mharis:3 dbms:postgresql

create table organizations
(
    id       uuid                      not null
        constraint organization_pk primary key,
    name    varchar                   not null,
    logo_location  varchar                     not null
);

create unique index organizations_id_uindex on organizations (id);
