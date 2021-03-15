drop schema if exists `rest-vacinas`;

create schema `rest-vacinas`;

use `rest-vacinas`;

set FOREIGN_KEY_CHECKS = 0;

drop table if exists `user`;

create table `user` (
    `name` varchar(128) not null,
    `cpf` varchar(14) not null,
    `email` varchar(60) not null,
    `birth_date` date not null,
    
    unique key (`cpf`),
    unique key (`email`),
    
    primary key(`cpf`)
) engine=InnoDB default charset=utf8;

drop table if exists `vaccine`;

create table `vaccine` (
    `id` int8 not null auto_increment,
    `name` varchar(128) not null,
    `date` date not null,
    `user_cpf` varchar(14) not null,
    primary key(`id`),
    key `FK_USER_idx` (`user_cpf`),
    constraint `FK_USER` foreign key (`user_cpf`)
    references `user` (`cpf`) 
    on delete no action on update no action
) engine=InnoDB auto_increment=100 default charset=utf8;

