--liquibase formatted sql

--changeset orkg:24 dbms:postgresql

-- Drop the existing ties to the "users" table

alter table organizations
    drop constraint if exists organizations_users_id_fk;

-- Add new constraints between contributors and observatories / organizations

alter table contributors
    add constraint contributors_observatories_id_fk
        foreign key (observatory_id) references observatories (id);

alter table contributors
    add constraint contributors_organizations_id_fk
        foreign key (organization_id) references organizations (id);
