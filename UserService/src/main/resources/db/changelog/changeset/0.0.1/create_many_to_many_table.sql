--liquibase formatted sql
--changeset Alexandr:2
--comment create many to many tables

create table if not exists roles(
    id integer primary key generated always as identity,
    name varchar(32)
);

create table if not exists user_roles(
    user_id integer,
    role_id integer,
    primary key (user_id,role_id)
);

insert into roles(name) values ('ROLE_MANAGER'),('ROLE_USER');
ALTER TABLE users ADD CONSTRAINT uk_users_keycloak_id UNIQUE (keycloak_id);

