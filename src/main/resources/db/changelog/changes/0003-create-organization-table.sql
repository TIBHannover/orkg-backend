--liquibase formatted sql

--changeset mharis:3 dbms:postgresql

create table organizations
(
	id         uuid   not null
		constraint organization_pk primary key,
	name       varchar not null,
	created_by uuid    not null
		constraint organizations_users_id_fk references users
);

create unique index organizations_id_uindex on organizations (id);

