--liquibase formatted sql
--changeset Alexandr:add_data_to_project_statuses
--comment add_data

insert into project_statuses(status) values ('Создан'),
                                            ('В процессе'),
                                            ('Остановлен'),
                                            ('Завершен'),
                                            ('Срок выполнения истёк')


