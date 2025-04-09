--liquibase formatted sql
--changeset Alexandr:add_data_to_statuses
--comment add_data

insert into statuses(status) values ('Создан'),
                                  ('В процессе'),
                                  ('Остановлен'),
                                  ('Завершен'),
                                  ('Срок выполнения истёк')