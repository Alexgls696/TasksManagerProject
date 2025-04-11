--liquibase formatted sql
--changeset Alexandr:create projects_database_tables
--comment first migration

create table if not exists project_statuses(
    id integer primary key generated always as identity,
    status varchar(255)
);

create table if not exists projects(
    id integer primary key generated always as identity,
    name varchar,
    description varchar(255),
    creation_date timestamp,
    deadline timestamp,
    creator_id integer,
    status_id integer references project_statuses(id)
);

create table if not exists project_members(
    project_id integer references projects(id),
    user_id integer,
    primary key (project_id,user_id)
);

create or replace function getProjectsByMemberId(memberId integer)
    returns table
            (
                id            integer,
                name          varchar(64),
                description   varchar(255),
                creation_date timestamp,
                deadline      timestamp,
                creator_id    integer,
                status_id     integer
            )
as
$$
begin
    return query select p.id,p.name,p.description,p.creation_date,p.deadline,p.creator_id,p.status_id from projects p
                                                                                                               join public.project_members pm on p.id = pm.project_id
                 where pm.user_id = memberId;
end
$$ language plpgsql;
