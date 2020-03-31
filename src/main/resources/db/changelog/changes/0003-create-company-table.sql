--liquibase formatted sql

--changeset mprinz:2 dbms:postgresql

create table companies
(
    id       uuid                      not null
        constraint company_pk primary key,
    name    varchar                   not null,
    logo  varchar                     not null
);

create unique index companies_id_cindex on companies (id);
