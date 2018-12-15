create table if not exists services
(
	id uuid default uuid_generate_v4() not null
		constraint services_pkey
			primary key,
	name varchar(255) not null,
	created_at timestamp default now() not null,
	updated_at timestamp default now() not null,
	enabled boolean default true not null,
	private_key varchar(255) not null,
	order_id bigserial not null
)
;

create index if not exists services_order_id_index
	on services (order_id)
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
	external_id varchar(36) default uuid_generate_v4() not null,
	order_id bigserial not null
)
;

create index if not exists users_service_id_foreign_id_index
	on users (service_id, external_id)
;

create index if not exists users_service_id_index
	on users (service_id)
;

create index if not exists users_order_id_index
	on users (order_id)
;

create table if not exists channels
(
	id uuid default uuid_generate_v4() not null
		constraint channels_pkey
			primary key,
	service_id uuid not null
		constraint channels_services_id_fk
			references services,
	external_id varchar(36) default uuid_generate_v4() not null,
	title varchar(255) not null,
	updated_at timestamp default now() not null,
	enabled boolean default true not null,
	created_at timestamp default now() not null,
	attributes text,
	order_id bigserial not null
)
;

create unique index if not exists channels_order_id_uindex
	on channels (order_id)
;

create index if not exists channels_service_id_foreign_id_index
	on channels (service_id, external_id)
;

create index if not exists channels_service_id_index
	on channels (service_id)
;

create table if not exists messages
(
	id uuid default uuid_generate_v4() not null
		constraint messages_pkey
			primary key,
	service_id uuid not null,
	channel_id uuid not null,
	user_id uuid not null,
	attributes text,
	message text not null,
	is_silent boolean default false not null,
	created_at timestamp default now() not null,
	updated_at timestamp default now() not null,
	enabled boolean default true not null,
	order_id bigserial not null
)
;

create index if not exists messages_channel_id_order_id_index
	on messages (channel_id asc, order_id desc)
;
