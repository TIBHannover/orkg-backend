
--liquibase formatted sql

--changeset mharis:10 dbms:postgresql

--adding column to handle named URLs
alter table organizations
    add display_id varchar not null default 'randomText';

--replacing default value with name of organization, also replacing spaces and commas with dashes ( - )
update organizations set display_id=LOWER(replace(replace(organizations.name,' ','-'), '''',''));


create unique index organizations_display_id_uindex on organizations (display_id);
