--liquibase formatted sql

--changeset orkg:11 dbms:postgresql

INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_USER')  ON CONFLICT DO NOTHING;
