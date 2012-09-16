# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table book (
  id                        varchar(255) not null,
  isbn                      varchar(255),
  name                      varchar(255),
  status                    varchar(17),
  create_date               datetime,
  constraint ck_book_status check (status in ('BUYING_PROCESSING','RENEW','RENT','AVAILABLE')),
  constraint pk_book primary key (id))
;

create table rental (
  id                        bigint auto_increment not null,
  user_id                   varchar(255) not null,
  book_id                   varchar(255),
  rental_date               datetime,
  return_date               datetime,
  status                    varchar(6),
  constraint ck_rental_status check (status in ('RENTAL','RETURN')),
  constraint pk_rental primary key (id))
;

create table user (
  id                        varchar(255) not null,
  name                      varchar(255),
  constraint pk_user primary key (id))
;

alter table rental add constraint fk_rental_user_1 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_rental_user_1 on rental (user_id);
alter table rental add constraint fk_rental_book_2 foreign key (book_id) references book (id) on delete restrict on update restrict;
create index ix_rental_book_2 on rental (book_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table book;

drop table rental;

drop table user;

SET FOREIGN_KEY_CHECKS=1;

