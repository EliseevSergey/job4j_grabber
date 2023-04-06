create table if not exists post (
id serial primary key,
name TEXT,
text TEXT,
link TEXT unique,
created TIMESTAMP
);