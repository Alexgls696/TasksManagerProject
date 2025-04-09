--liquibase formatted sql
--changeset Alexandr:add_data_to_categories
--comment add_data_to_categories

insert into task_categories (name) values ('Работа'),('Учеба'),('Дом');