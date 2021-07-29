--liquibase formatted sql

--changeset mharis:5 dbms:postgresql

create table user_observatories
(
    user_id          uuid             not null
        constraint user_id_fk references users,
    observatory_id   uuid             not null
        constraint observatories_id_fk references observatories
);
