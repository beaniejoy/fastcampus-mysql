create table Member
(
    id int auto_increment,
    email varchar(20) not null,
    nickname varchar(20) not null,
    birthday date not null,
    createdAt datetime not null,
    constraint member_id_uindex
        primary key (id)
);

create table MemberNicknameHistory
(
    id int auto_increment,
    memberId int not null,
    nickname varchar(20) not null,
    createdAt datetime not null,
    constraint memberNicknameHistory_id_uindex
        primary key (id)
);

create table Follow
(
    id int auto_increment,
    fromMemberId int not null,
    toMemberId int not null,
    createdAt datetime not null,
    constraint Follow_id_uindex
        primary key (id)
);

create unique index Follow_fromMemberId_toMemberId_uindex
    on Follow (fromMemberId, toMemberId);


create table Post
(
    id int auto_increment,
    memberId int not null,
    contents varchar(100) not null,
    createdDate date not null,
    createdAt datetime not null,
    constraint POST_id_uindex
        primary key (id)
);

create index POST__index_member_id
    on Post (memberId);

create index POST__index_created_date
    on Post (createdDate);

create index POST__index_member_id_created_date
    on Post (memberId, createdDate);

create table Timeline
(
    id int auto_increment,
    memberId int not null,
    postId int not null,
    createdAt datetime not null,
    constraint Timeline_id_uindex
        primary key (id)
);

create index Timeline__index_member_id_post_id
    on Timeline (memberId, postId);

create table PostLike
(
    id int auto_increment,
    memberId int not null,
    postId int not null,
    createdAt datetime not null,
    constraint PostLike_id_uindex
        primary key (id)
);

-- Post 테이블에 likeCount 추가
alter table Post add column likeCount int;
-- 낙관적 락을 위한 version 필드 추가
alter table Post add column version int default 0;

create table PostLike
(
    id int auto_increment,
    memberId int not null,
    postId int not null,
    createdAt datetime not null,
    constraint PostLike_id_uindex
        primary key (id)
);