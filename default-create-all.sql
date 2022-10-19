create table email_error (
  id                            bigserial not null,
  error_key                     varchar(255),
  sent_to_email_address         varchar(255),
  last_sent                     timestamptz,
  send_count                    integer not null,
  constraint uq_email_error_error_key unique (error_key),
  constraint pk_email_error primary key (id)
);

create table invitation (
  id                            bigserial not null,
  email_address                 varchar(255),
  inviting_user_id              bigint,
  invitation_date               timestamptz,
  is_accepted                   boolean,
  constraint pk_invitation primary key (id)
);

create table invoice (
  id                            bigserial not null,
  user_id                       bigint not null,
  invoice_date                  timestamptz not null,
  payment_date                  timestamptz,
  bitpay_url                    varchar(255),
  bitpay_id                     varchar(255),
  constraint pk_invoice primary key (id)
);

create table invoice_line (
  id                            bigserial not null,
  parent_invoice_id             bigint not null,
  name                          varchar(255),
  description                   varchar(255),
  price                         decimal(38),
  quantity                      integer,
  constraint pk_invoice_line primary key (id)
);

create table job_event (
  id                            bigserial not null,
  successful                    boolean,
  stack_trace                   text,
  start_date                    timestamptz,
  end_date                      timestamptz,
  duration_milliseconds         bigint,
  job_class                     varchar(255),
  job_title                     varchar(255),
  constraint pk_job_event primary key (id)
);

create table node (
  id                            bigserial not null,
  name                          varchar(255),
  ip_address                    varchar(255),
  scheme                        varchar(255),
  download_ip_address           varchar(255),
  download_scheme               varchar(255),
  api_key                       varchar(255),
  certificates_to_trust         varchar(255),
  down                          boolean,
  active                        boolean,
  status                        json,
  constraint pk_node primary key (id)
);

create table plan (
  id                            bigserial not null,
  name                          varchar(255),
  max_diskspace_gb              integer,
  max_active_torrents           integer,
  monthly_cost                  decimal(38),
  visible                       boolean,
  totalslots                    integer,
  constraint pk_plan primary key (id)
);

create table torrent (
  id                            bigserial not null,
  torrent_hash                  varchar(255),
  name                          varchar(255),
  metadata_percent_complete     float,
  percent_complete              float,
  download_speed_bytes          bigint,
  upload_speed_bytes            bigint,
  downloaded_bytes              bigint,
  uploaded_bytes                bigint,
  total_size_bytes              bigint,
  zip_download_link             varchar(255),
  error                         varchar(255),
  state                         varchar(20),
  create_date                   timestamptz,
  node_id                       bigint,
  constraint ck_torrent_state check ( state in ('METADATA_DOWNLOADING','DOWNLOADING','PAUSED','SEEDING','ERROR','QUEUED','CHECKING')),
  constraint pk_torrent primary key (id)
);

create table torrent_event (
  id                            bigserial not null,
  successful                    boolean,
  stack_trace                   text,
  start_date                    timestamptz,
  end_date                      timestamptz,
  duration_milliseconds         bigint,
  torrent_hash                  varchar(255),
  event_type                    varchar(8),
  user_notified                 boolean,
  constraint ck_torrent_event_event_type check ( event_type in ('ADDING','REMOVING')),
  constraint pk_torrent_event primary key (id)
);

create table users (
  id                            bigserial not null,
  email_address                 varchar(255),
  open_id                       varchar(255),
  admin                         boolean,
  avatar_url                    varchar(255),
  display_name                  varchar(255),
  last_access                   timestamptz,
  api_key                       varchar(255),
  plan_id                       bigint,
  dedicated_node_id             bigint,
  constraint uq_users_email_address unique (email_address),
  constraint pk_users primary key (id)
);

create table user_message (
  id                            bigserial not null,
  state                         varchar(7) not null,
  heading                       varchar(255) not null,
  message                       text not null,
  user_id                       bigint not null,
  create_date                   timestamptz not null,
  retrieved                     boolean not null,
  constraint ck_user_message_state check ( state in ('MESSAGE','ERROR')),
  constraint pk_user_message primary key (id)
);

create table torrent_group (
  id                            bigserial not null,
  group_name                    varchar(255),
  user_id                       bigint not null,
  torrent_hash_id               bigint not null,
  paused                        boolean,
  running                       boolean,
  constraint pk_torrent_group primary key (id)
);

alter table invitation add constraint fk_invitation_inviting_user_id foreign key (inviting_user_id) references users (id) on delete restrict on update restrict;
create index ix_invitation_inviting_user_id on invitation (inviting_user_id);

alter table invoice add constraint fk_invoice_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_invoice_user_id on invoice (user_id);

alter table invoice_line add constraint fk_invoice_line_parent_invoice_id foreign key (parent_invoice_id) references invoice (id) on delete restrict on update restrict;
create index ix_invoice_line_parent_invoice_id on invoice_line (parent_invoice_id);

alter table torrent add constraint fk_torrent_node_id foreign key (node_id) references node (id) on delete restrict on update restrict;
create index ix_torrent_node_id on torrent (node_id);

alter table users add constraint fk_users_plan_id foreign key (plan_id) references plan (id) on delete restrict on update restrict;
create index ix_users_plan_id on users (plan_id);

alter table users add constraint fk_users_dedicated_node_id foreign key (dedicated_node_id) references node (id) on delete restrict on update restrict;
create index ix_users_dedicated_node_id on users (dedicated_node_id);

alter table user_message add constraint fk_user_message_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_user_message_user_id on user_message (user_id);

alter table torrent_group add constraint fk_torrent_group_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_torrent_group_user_id on torrent_group (user_id);

alter table torrent_group add constraint fk_torrent_group_torrent_hash_id foreign key (torrent_hash_id) references torrent (id) on delete restrict on update restrict;
create index ix_torrent_group_torrent_hash_id on torrent_group (torrent_hash_id);

