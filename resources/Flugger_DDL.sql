create table if not exists services
(
	id uuid default uuid_generate_v4() not null
		constraint services_pkey
			primary key,
	name varchar(255) not null,
	created_at timestamp default now() not null,
	updated_at timestamp default now() not null,
	enabled boolean default true not null
)
;

alter table services owner to flugger
;

create table if not exists users
(
	id uuid default uuid_generate_v4() not null
		constraint users_pkey
			primary key,
	service_id uuid not null
		constraint users_services_id_fk
			references services,
	name varchar(255) not null,
	attributes text,
	updated_at timestamp default now() not null,
	enabled boolean default true,
	created_at timestamp default now() not null,
	foreign_id varchar(36) default uuid_generate_v4() not null
)
;

alter table users owner to flugger
;

create index if not exists users_service_id_index
	on users (service_id)
;

create index if not exists users_service_id_foreign_id_index
	on users (service_id, foreign_id)
;

create table if not exists channels
(
	id uuid default uuid_generate_v4() not null
		constraint channels_pkey
			primary key,
	service_id uuid not null
		constraint channels_services_id_fk
			references services,
	foreign_id varchar(36) default uuid_generate_v4() not null,
	title varchar(255) not null,
	updated_at timestamp default now() not null,
	enabled boolean default true not null,
	created_at timestamp default now() not null,
	attributes text
)
;

alter table channels owner to flugger
;

create index if not exists channels_service_id_index
	on channels (service_id)
;

create index if not exists channels_service_id_foreign_id_index
	on channels (service_id, foreign_id)
;

