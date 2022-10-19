alter table if exists invitation drop constraint if exists fk_invitation_inviting_user_id;
drop index if exists ix_invitation_inviting_user_id;

alter table if exists invoice drop constraint if exists fk_invoice_user_id;
drop index if exists ix_invoice_user_id;

alter table if exists invoice_line drop constraint if exists fk_invoice_line_parent_invoice_id;
drop index if exists ix_invoice_line_parent_invoice_id;

alter table if exists torrent drop constraint if exists fk_torrent_node_id;
drop index if exists ix_torrent_node_id;

alter table if exists users drop constraint if exists fk_users_plan_id;
drop index if exists ix_users_plan_id;

alter table if exists users drop constraint if exists fk_users_dedicated_node_id;
drop index if exists ix_users_dedicated_node_id;

alter table if exists user_message drop constraint if exists fk_user_message_user_id;
drop index if exists ix_user_message_user_id;

alter table if exists torrent_group drop constraint if exists fk_torrent_group_user_id;
drop index if exists ix_torrent_group_user_id;

alter table if exists torrent_group drop constraint if exists fk_torrent_group_torrent_hash_id;
drop index if exists ix_torrent_group_torrent_hash_id;

drop table if exists email_error cascade;

drop table if exists invitation cascade;

drop table if exists invoice cascade;

drop table if exists invoice_line cascade;

drop table if exists job_event cascade;

drop table if exists node cascade;

drop table if exists plan cascade;

drop table if exists torrent cascade;

drop table if exists torrent_event cascade;

drop table if exists users cascade;

drop table if exists user_message cascade;

drop table if exists torrent_group cascade;

