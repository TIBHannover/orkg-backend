--liquibase formatted sql

--changeset mharis:12 dbms:postgresql

--adding column to handle discourse discussion topic Id
alter table observatories
    add topic_id integer;


