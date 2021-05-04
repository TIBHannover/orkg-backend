--liquibase formatted sql

--changeset mharis:10 dbms:postgresql

--adding column to handle named URLs
alter table observatories
    add display_id varchar;

--replacing default value with name of observatories
update observatories set display_id=replace(replace(observatories.name,' ','_'), '''','');

alter table observatories alter column display_id set not null;
create unique index observatories_display_id_uindex on observatories (display_id);

