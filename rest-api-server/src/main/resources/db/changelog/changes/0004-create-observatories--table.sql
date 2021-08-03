--liquibase formatted sql

--changeset mharis:4 dbms:postgresql

create table observatories
(
    id                uuid                 not null
        constraint observatory_pk primary key,
    name              varchar              not null,
    organization_id   uuid                 not null
        constraint observatories_organzations_id_fk references organizations
);
create unique index observatories_id_uindex on observatories (id);
