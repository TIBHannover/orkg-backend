--liquibase formatted sql

--changeset mprinz:3 dbms:postgresql

create table organizations
(
    id       uuid                      not null
        constraint organization_pk primary key,
    name    varchar                   not null,
    logo  varchar                     not null
);

create unique index organization_id_oindex on organizations (id);
