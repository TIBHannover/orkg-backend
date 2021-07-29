--liquibase formatted sql

--changeset mharis:7 dbms:postgresql

--adding column in observatories table to handle descriptions
alter table observatories
	add description varchar;

-- add column in organizations table to handle URLs
alter table organizations
	add url varchar;
