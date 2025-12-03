--liquibase formatted sql

--changeset orkg:34 dbms:postgresql

alter table contributors
    rename column email_md5 to email_multihash;

--remove character limit
alter table contributors
    alter column email_multihash type character varying;

--convert existing md5 values to multihash values
update contributors
    set email_multihash = concat('d50110', email_multihash)
