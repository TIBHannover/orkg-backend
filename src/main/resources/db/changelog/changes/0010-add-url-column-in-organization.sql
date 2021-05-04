
--liquibase formatted sql

--changeset mharis:10 dbms:postgresql

--adding column to handle named URLs
alter table organizations
    add display_id varchar;

--replacing default value with name of organization, also replacing spaces and commas with dashes ( - )
update organizations set display_id=replace(replace(organizations.name,' ','_'), '''','');

alter table organizations alter column display_id set not null;
create unique index organizations_display_id_uindex on organizations (display_id);
