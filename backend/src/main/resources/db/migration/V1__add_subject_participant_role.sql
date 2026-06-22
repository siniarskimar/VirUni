create table participant_role(
    id bigint not null,
    name varchar(255) not null unique,
    primary key(id)
);

insert into participant_role(id, name) values
(1, 'LEADING_TEACHER'),
(2, 'TEACHER'),
(3, 'STUDENT'),
(4, 'GUEST');

alter table if exists subject_participants
    add column role_id bigint not null;

alter table if exists subject_participants
    add constraint subject_participants_role_id_fkey
    foreign key (role_id) references participant_role;
