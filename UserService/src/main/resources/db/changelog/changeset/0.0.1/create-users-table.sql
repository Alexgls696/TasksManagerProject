--liquibase formatted sdq

--changeset Alexandr:create_users_table
--comment first_migration create users table

create table if not exists users
(
    id                integer generated always as identity
        primary key,
    name              varchar(50)  default 'Unknown'::character varying not null,
    surname           varchar(50)  default 'Unknown'::character varying not null,
    username          varchar(64)  default 'Unknown'::character varying not null,
    email             varchar(255) default 'Unknown'::character varying not null,
    keycloak_id       varchar(255),
    created_timestamp bigint
);