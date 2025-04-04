--liquibase formatted sql
--changeset Alexander:fixes

--comment add email column in table users and insert first users

alter table users add column email varchar(255) not null default 'Unknown';

insert into users (name,surname,username,email)
values ('Александр','Глущенко','alexgls','glualex11@gmail.com')
