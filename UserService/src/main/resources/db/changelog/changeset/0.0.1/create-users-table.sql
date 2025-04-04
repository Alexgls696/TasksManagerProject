--liquibase formatted sdq

--changeset Alexandr:create_users_table
--comment first_migration create users table

create table if not exists users(
    id integer primary key generated always as identity,
    name varchar(50) not null default 'Unknown',
    surname varchar(50) not null default 'Unknown',
    username varchar(64) not null default 'Unknown'
);