--liquibase formatted sql

--changeset orkg:20 dbms:postgresql

-- NOTE:
--   We do not care about referential consistency here, because this information will come from a different service and
--   cannot be verified easily. It is verified by the service, the adapter does not need to concern itself with that.

alter table images
    drop constraint if exists uploaded_by_fk;
