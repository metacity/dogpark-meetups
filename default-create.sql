create table city (
  id                        bigint auto_increment not null,
  name                      varchar(255) not null,
  constraint pk_city primary key (id))
;

create table dogpark (
  id                        bigint auto_increment not null,
  name                      varchar(255) not null,
  latitude                  double not null,
  longitude                 double not null,
  city_id                   bigint,
  constraint pk_dogpark primary key (id))
;

create table dogpark_signup (
  id                        bigint auto_increment not null,
  arrival_time              datetime not null,
  dog_breed                 varchar(255),
  dog_weight                integer not null,
  cancellation_code         varchar(255) not null,
  dogpark_id                bigint,
  constraint pk_dogpark_signup primary key (id))
;

alter table dogpark add constraint fk_dogpark_city_1 foreign key (city_id) references city (id) on delete restrict on update restrict;
create index ix_dogpark_city_1 on dogpark (city_id);
alter table dogpark_signup add constraint fk_dogpark_signup_dogPark_2 foreign key (dogpark_id) references dogpark (id) on delete restrict on update restrict;
create index ix_dogpark_signup_dogPark_2 on dogpark_signup (dogpark_id);


