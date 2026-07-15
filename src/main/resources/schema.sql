create database "authdb"
with encoding "UTF8";

create table Users(
    id uuid,
    username varchar(32) not null unique,
    password varchar(128) not null,
    first_name varchar(32) not null,
    last_name varchar(32) not null,
    email varchar(48) not null unique,
    created_at timestamp without time zone default now(),
    constraint user_pkey primary key (id)
);

create table Roles(
    id serial primary key,
    name varchar(48) not null unique
);

create table Users_Roles(
    user_id uuid not null,
    role_id integer not null,
    primary key (user_id,role_id),
    constraint user_fkey foreign key (user_id) references Users(id) on delete cascade,
    constraint role_fkey foreign key (role_id) references Roles(id) on delete cascade
);