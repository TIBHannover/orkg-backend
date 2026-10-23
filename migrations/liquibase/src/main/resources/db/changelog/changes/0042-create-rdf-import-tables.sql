--liquibase formatted sql

--changeset orkg:42 dbms:postgresql

CREATE TABLE rdf_files
(
    id                              UUID    NOT NULL,
    name                            VARCHAR NOT NULL,
    base_uri                        VARCHAR,
    format                          VARCHAR NOT NULL,
    state                           VARCHAR NOT NULL,
    validation_job_id               VARCHAR,
    import_job_id                   VARCHAR,
    data                            VARCHAR NOT NULL,
    data_md5                        VARCHAR NOT NULL,
    created_by                      UUID    NOT NULL,
    created_at                      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at_offset_total_seconds INTEGER NOT NULL,
    CONSTRAINT pk_rdf_files PRIMARY KEY (id)
);

CREATE TABLE rdf_triples
(
    id           UUID    NOT NULL,
    rdf_file_id  UUID    NOT NULL,
    subject      VARCHAR NOT NULL,
    subject_type VARCHAR NOT NULL,
    predicate    VARCHAR NOT NULL,
    object       VARCHAR NOT NULL,
    object_type  VARCHAR NOT NULL,
    CONSTRAINT pk_rdf_triple PRIMARY KEY (id)
);

CREATE TABLE rdf_classes
(
    id          UUID    NOT NULL,
    iri         VARCHAR NOT NULL,
    rdf_file_id UUID    NOT NULL,
    label       VARCHAR,
    CONSTRAINT pk_rdf_classes PRIMARY KEY (id)
);

CREATE TABLE rdf_predicates
(
    id          UUID    NOT NULL,
    iri         VARCHAR NOT NULL,
    rdf_file_id UUID    NOT NULL,
    label       VARCHAR,
    CONSTRAINT pk_rdf_predicates PRIMARY KEY (id)
);

CREATE TABLE rdf_resources
(
    id          UUID    NOT NULL,
    iri         VARCHAR NOT NULL,
    rdf_file_id UUID    NOT NULL,
    label       VARCHAR,
    classes     TEXT[]  NOT NULL,
    CONSTRAINT pk_rdf_resources PRIMARY KEY (id)
);
