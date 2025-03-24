create table ihcs
(
    id             int auto_increment
        primary key,
    image_name     varchar(255)                            not null,
    positive_area  bigint                                  not null,
    total_area     bigint                                  not null,
    analysis_date  timestamp                               null,
    foldername     varchar(255)                            not null,
    positive_ratio decimal(5, 2) default 0.00              not null,
    username       varchar(255)                            not null,
    uploads_date   timestamp     default CURRENT_TIMESTAMP not null,
    constraint unique_folder_image
        unique (foldername, image_name)
);

create table users
(
    id       int auto_increment
        primary key,
    username varchar(50)  not null,
    password varchar(255) not null,
    constraint username
        unique (username)
);

