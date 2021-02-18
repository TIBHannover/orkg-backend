
--liquibase formatted sql

--changeset mharis:10 dbms:postgresql

--adding column to handle URLs with observatories name
alter table organizations
    add uri_name varchar;
