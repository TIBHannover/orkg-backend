--liquibase formatted sql

--changeset mharis:6 dbms:postgresql

create table observatory_organizations
(
	observatory_id  uuid not null
		constraint observatories_id_fk references observatories,
	organization_id uuid not null
		constraint organizations_id_fk references organizations
);

insert into observatory_organizations (observatory_id, organization_id)
select id, organization_id
from observatories;

alter table observatories
	drop constraint observatories_organzations_id_fk;

alter table observatories
	drop column organization_id;

alter table users
	add observatory_id uuid
		constraint observatories_id_fk references observatories;

alter table users
	add organization_id uuid
		constraint organizations_id_fk references organizations;

drop table user_observatories;
