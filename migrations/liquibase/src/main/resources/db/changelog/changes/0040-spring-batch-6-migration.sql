--liquibase formatted sql

--changeset orkg:40 dbms:postgresql

ALTER SEQUENCE BATCH_JOB_SEQ RENAME TO BATCH_JOB_INSTANCE_SEQ;
