--liquibase formatted sql

--changeset orkg:27 dbms:postgresql

DROP TABLE IF EXISTS users, roles, users_roles CASCADE;
