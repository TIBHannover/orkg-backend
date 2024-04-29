--liquibase formatted sql

--changeset orkg:9 dbms:postgresql

-- Normalize roles via an intermediate mapping table so they have a many-to-many relationship to users.
-- Since we can only generate UUIDs in PostgreSQL 13 we will use a regular "int" as a key for now until we are able
-- to upgrade. Ideally, we would use UUID everywhere.


-- Rename former "roles" table. It will become the join table.
alter table roles
    rename to users_roles;

-- Add a new column to contain the role's ID.
alter table users_roles
    add role_id int;

-- Create new "roles" table and constraints.
create table roles
(
    role_id serial  not null,
    name    varchar not null
);
create unique index roles_name_uindex
    on roles (name);
create unique index roles_role_id_uindex
    on roles (role_id);
alter table roles
    add constraint roles_pk
        primary key (role_id);

-- Fill new "roles" table with existing roles.
insert into roles (name)
select distinct name
from users_roles;

-- Add role IDs to "users_roles" table by mapping role names.
update users_roles
set role_id = roles.role_id
FROM roles
WHERE users_roles.name = roles.name;

-- Remove "name" column from "users_roles". Drop existing index first.
drop index roles_id_name_uindex;
alter table users_roles
    drop column name;

-- Rename column with user ID for consistency. (Do not touch users table.)
alter table users_roles
    drop constraint roles_users_id_fk;
alter table users_roles
    rename column id to user_id;

-- Create foreign key constraints.
alter table users_roles
    add constraint users_roles_roles_role_id_fk
        foreign key (role_id) references roles;

alter table users_roles
    add constraint users_roles_users_id_fk
        foreign key (user_id) references users;

-- Make sure all pairs of users and roles are unique.
create unique index users_roles_user_id_role_id_uindex
    on users_roles (user_id, role_id);
