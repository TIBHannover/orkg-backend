--liquibase formatted sql

--changeset orkg:10 dbms:postgresql

-- Create table to hold members of the observatory
create table observatory_members
(
    observatory_id uuid not null
        constraint observatory_members_observatories_id_fk
            references observatories,
    user_id        uuid not null
        constraint observatory_members_users_id_fk
            references users
);

-- Make sure the pairs are unique
create unique index observatory_members_observatory_id_user_id_uindex
    on observatory_members (observatory_id, user_id);

-- Migrate existing data
insert into observatory_members
select o.id as observatory_id, users.id as user_id
from users
         left join observatories o on users.observatory_id = o.id
where users.observatory_id is not null;

-- Remove old observatory_id foreign key constraint and column
alter table users drop constraint observatories_id_fk;

alter table users drop column observatory_id;
