--liquibase formatted sql

--changeset orkg:26 dbms:postgresql

CREATE TABLE keycloak_event_states
(
    event_type character varying NOT NULL,
    counter integer NOT NULL DEFAULT 0,
    PRIMARY KEY (event_type)
);

INSERT INTO keycloak_event_states values('ADMIN_EVENT', 0);
INSERT INTO keycloak_event_states values('USER_EVENT', 0);
