create table client_kie_session (session_identifier int8 not null, client_id varchar(255), created timestamp not null, updated timestamp, primary key (session_identifier));
alter table client_kie_session add constraint UK_4k47672ghf6ob1bf28sm2yjww unique (client_id);