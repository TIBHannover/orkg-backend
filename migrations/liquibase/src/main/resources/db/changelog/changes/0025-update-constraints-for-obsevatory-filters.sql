--liquibase formatted sql

--changeset orkg:25 dbms:postgresql

-- Drop the existing ties to the "users" table

alter table observatory_filters
    drop constraint if exists created_by_fk;

-- Add new constraints between observatory filters and contributors

alter table observatory_filters
    add constraint observatory_filters_contributors_id_fk
        foreign key (created_by) references contributors;
