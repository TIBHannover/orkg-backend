--liquibase formatted sql

--changeset orkg:13 dbms:postgresql

--adding doi column in organizations
alter table organizations
    add doi varchar;
