--liquibase formatted sql

--changeset mharis:6 dbms:postgresql

create table observatories_organizations
(
	observatory_id  uuid not null
		constraint observatories_id_fk references observatories,
	organization_id uuid not null
		constraint organizations_id_fk references organizations
);

--migrating observatories data into observatories_organizations to handle m:n relationship
insert into observatories_organizations (observatory_id, organization_id)
select id, organization_id
from observatories;

--dropping organization_id FK constraints and column
alter table observatories
	drop constraint observatories_organzations_id_fk;

alter table observatories
	drop column organization_id;

-- adding column observatory_id and organization_id to handle 1:m relationship between users and observatories
alter table users
	add observatory_id uuid
		constraint observatories_id_fk references observatories;

alter table users
	add organization_id uuid
		constraint organizations_id_fk references organizations;

drop table user_observatories;
