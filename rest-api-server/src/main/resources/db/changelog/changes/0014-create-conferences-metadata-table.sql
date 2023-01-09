--liquibase formatted sql

--changeset orkg:14 dbms:postgresql

--adding table conferences_series to handle series of each conference
create table conferences_series
(
    id uuid not null
        constraint conference_series_pk primary key,
    organization_id uuid not null
        constraint organizations_id_fk references organizations,
    name varchar not null,
    display_id varchar not null,
    url varchar not null,
    start_date DATE not null,
    review_type varchar not null
);

create unique index conferences_series_id_uindex on conferences_series (id);

--drop relation between organizations and conferences_metadata table
alter table conferences_metadata
    drop constraint organizations_id_fk;

drop table conferences_metadata;
