--liquibase formatted sql
--changeset Alexandr:stored_functions_and_new_tables
--comment add functions and many-to-many tasks table

create or replace function getProjectByUserId(userId integer)
    returns table
            (
                id            integer,
                name          varchar(255),
                description   varchar(255),
                creation_date timestamp(6),
                deadline      timestamp(6),
                creator_id    integer,
                status_id     integer
            )
as
$$
begin
    return query
        (select p.id, p.name, p.description, p.creation_date, p.deadline, p.creator_id, p.status_id
         from projects p
                  join project_members pm on p.id = pm.project_id
         where user_id = userId);
end
$$ language plpgsql;


CREATE OR REPLACE FUNCTION getTasksAndCheckThemDeadline(projectId integer)
    RETURNS TABLE (
                      assignee_id integer,
                      category_id integer,
                      creator_id  integer,
                      id          integer,
                      priority    integer,
                      project_id  integer,
                      status_id   integer,
                      deadline    timestamp(6),
                      start_date  timestamp(6),
                      update_date timestamp(6),
                      description text,
                      title       varchar(255)
                  )
    LANGUAGE plpgsql
AS $$
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

--Для многие ко многим в задачах, аналогично проектам
create table if not exists tasks_members(
                                            task_id integer,
                                            user_id integer,
                                            primary key(task_id,user_id)
);


