create table calcom_booking_to_asker
(
    asker_id        varchar(255) not null,
    calcombookingid bigint       not null
);

create table calcom_user_to_consultant
(
    consultantid varchar(255) not null
        primary key,
    calcomuserid bigint       not null,
    constraint calcomuserid
        unique (calcomuserid)
);

create table team_to_agency
(
    teamid   bigint not null
        primary key,
    agencyid bigint not null
);