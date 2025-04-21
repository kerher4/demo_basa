-- liquibase formatted sql

-- changeset kerher:1
insert into users(password, username, role) values ('{noop}1234', 'admin', 'ADMIN');