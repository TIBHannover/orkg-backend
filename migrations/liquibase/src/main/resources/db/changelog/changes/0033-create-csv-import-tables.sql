--liquibase formatted sql

--changeset orkg:32 dbms:postgresql

CREATE TABLE typed_csv_records
(
    id uuid NOT NULL,
    csv_id uuid NOT NULL,
    item_number bigint NOT NULL,
    line_number bigint NOT NULL,
    "values" bytea NOT NULL,
    PRIMARY KEY (id)
);

create index typed_csv_records_item_number_index on typed_csv_records (item_number);
create index typed_csv_records_csv_id_index on typed_csv_records (csv_id);

CREATE TABLE paper_csv_records
(
    id uuid NOT NULL,
    csv_id uuid NOT NULL,
    item_number bigint NOT NULL,
    line_number bigint NOT NULL,
    title character varying NOT NULL,
    authors bytea NOT NULL,
    publication_month integer,
    publication_year bigint,
    published_in character varying,
    url character varying,
    doi character varying,
    research_field_id character varying NOT NULL,
    extraction_method character varying NOT NULL,
    statements bytea NOT NULL,
    PRIMARY KEY (id)
);

create index paper_csv_records_item_number_index on paper_csv_records (item_number);
create index paper_csv_records_csv_id_index on paper_csv_records (csv_id);

CREATE TABLE paper_csv_import_result_records
(
    id uuid NOT NULL,
    imported_entity_id character varying NOT NULL,
    imported_entity_type character varying NOT NULL,
    csv_id uuid NOT NULL,
    item_number bigint NOT NULL,
    line_number bigint NOT NULL,
    PRIMARY KEY (id)
);

create index paper_csv_import_result_records_item_number_index on paper_csv_import_result_records (item_number);
create index paper_csv_import_result_records_csv_id_index on paper_csv_import_result_records (csv_id);
