create table if not exists post (
id serial primary key,
name TEXT,
link TEXT unique,
created TIMESTAMP
);