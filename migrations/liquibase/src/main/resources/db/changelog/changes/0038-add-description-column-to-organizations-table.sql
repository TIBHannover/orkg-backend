--liquibase formatted sql

--changeset orkg:38 dbms:postgresql

alter table organizations
    add description varchar(8164);
