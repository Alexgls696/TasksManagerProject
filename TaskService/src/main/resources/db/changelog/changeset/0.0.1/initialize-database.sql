--liquibase formatted sql

--changeset Alexandr:1
--comment first migration

create table if not exists statuses(
    id integer primary key generated by default as identity ,
    status varchar(255)
);

insert into statuses(id, status) values (1,'Создана'),
                                    (2,'В процессе'),
                                    (3,'Остановлена'),
                                    (4,'Завершена'),
                                    (5,'Срок выполнения истёк');

create table if not exists task_categories(
    id integer primary key generated by default as identity,
    name varchar(255)
);
insert into task_categories (id,name) values (1,'Работа'),(2,'Учеба'),(3,'Дом');

create table if not exists tasks(
    id integer primary key generated always as identity,
    title varchar(255),
    description text,
    start_date timestamp,
    update_date timestamp,
    deadline timestamp,
    priority integer,
    assignee_id integer,
    creator_id integer,
    project_id integer,
    category_id integer references task_categories(id),
    status_id integer references statuses(id)
);

create table if not exists tasks_members(
    task_id integer,
    user_id integer,
    primary key(task_id,user_id)
);

create or replace function gettasksandcheckthemdeadline(projectid integer)
    returns TABLE(assignee_id integer, category_id integer, creator_id integer, id integer, priority integer, project_id integer, status_id integer, deadline timestamp without time zone, start_date timestamp without time zone, update_date timestamp without time zone, description text, title character varying)
    language plpgsql
as
$$
BEGIN
    -- Обновляем статусы просроченных задач (status_id = 5)
    UPDATE tasks
    SET status_id = 5
    WHERE
        tasks.project_id = projectId
      AND now() >= tasks.deadline
      AND tasks.status_id != 5;  -- Явно указываем таблицу

    -- Возвращаем все задачи проекта
    RETURN QUERY
        SELECT
            t.assignee_id,
            t.category_id,
            t.creator_id,
            t.id,
            t.priority,
            t.project_id,
            t.status_id,
            t.deadline,
            t.start_date,
            t.update_date,
            t.description,
            t.title
        FROM tasks t
        WHERE t.project_id = projectId;
END
$$;