-- liquibase formatted sql

-- changeset kerher:1
alter table users add column password varchar(300) not null;