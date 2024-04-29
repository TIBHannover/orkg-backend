--liquibase formatted sql

--changeset orkg:21 dbms:postgresql

create table observatories_sdgs
(
    observatory_id uuid not null
		constraint observatories_id_fk references observatories,
    sdg_id varchar not null
);
