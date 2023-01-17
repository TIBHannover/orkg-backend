--liquibase formatted sql

--changeset orkg:13 dbms:postgresql

create EXTENSION if not exists citext;
    alter table users alter column email type citext;
