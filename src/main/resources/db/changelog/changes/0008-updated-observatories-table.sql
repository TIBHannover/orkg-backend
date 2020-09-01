--liquibase formatted sql

--changeset mharis:8 dbms:postgresql

--adding column to handle observatories grouping
alter table observatories
	add research_field varchar;
