--liquibase formatted sql

--changeset orkg:17 dbms:postgresql

--remove not null constraint from created_by column in images table
alter table images
    alter column created_by
        drop not null;
