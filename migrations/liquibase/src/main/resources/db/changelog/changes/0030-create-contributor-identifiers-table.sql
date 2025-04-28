--liquibase formatted sql

--changeset orkg:30 dbms:postgresql

CREATE TABLE contributor_identifiers
(
    id uuid NOT NULL,
    contributor_id uuid NOT NULL,
    type character varying NOT NULL,
    value character varying NOT NULL,
    created_at timestamp with time zone NOT NULL,
    created_at_offset_total_seconds integer NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT contributor_identifiers_contributor_id_fk FOREIGN KEY (contributor_id)
        REFERENCES contributors (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
);

create unique index contributor_identifiers_contributor_id_value_uindex on contributor_identifiers (contributor_id, value);
