create table if not exists users
(
    id         serial
        primary key,
    phone      varchar(10) not null
        unique,
    name       varchar(100),
    role       varchar(10),
    status     varchar(20) default 'active'::character varying,
    created_at timestamp   default now()
);

create table if not exists patient_personal
(
    id                serial
        primary key,
    user_id           integer
        references users
            on delete cascade,
    email             varchar(100),
    gender            varchar(10),
    dob               varchar(20),
    blood_group       varchar(5),
    marital_status    varchar(20),
    height            varchar(10),
    weight            varchar(10),
    emergency_contact varchar(10),
    profile_photo     text
);

create table if not exists patient_medical
(
    id                  serial
        primary key,
    user_id             integer
        references users
            on delete cascade,
    allergies           text,
    current_medications text,
    past_medications    text,
    chronic_diseases    text,
    injuries            text,
    surgeries           text
);

create table if not exists patient_lifestyle
(
    id              serial
        primary key,
    user_id         integer
        unique
        references users
            on delete cascade,
    smoking         varchar(50),
    alcohol         varchar(50),
    activity_level  varchar(50),
    food_preference varchar(50),
    occupation      varchar(100)
);

create table if not exists doctor_personal
(
    id             serial primary key,
    user_id        integer references users on delete cascade,
    email          varchar(100),
    gender         varchar(10),
    dob            varchar(20),
    marital_status varchar(20),
    profile_photo  text,
    about          text
);

create table if not exists doctor_professional
(
    id                  serial primary key,
    user_id             integer references users on delete cascade,
    license_number      varchar(50) unique not null,
    speciality          varchar(100),
    sub_speciality      varchar(100),
    qualification       varchar(200),
    experience_years    integer,
    languages           text,
    consultation_fee    numeric(10, 2),
    consultation_type   varchar(20) default 'both'  -- ← new: 'online' | 'offline' | 'both'
);

create table if not exists doctor_clinic
(
    id          serial primary key,
    user_id     integer references users on delete cascade,
    clinic_name varchar(200),
    address     text,
    city        varchar(100),
    pincode     varchar(10),
    lat         numeric(10, 7),
    lng         numeric(10, 7)
);

create table if not exists doctor_availability
(
    id                    serial primary key,
    user_id               integer references users on delete cascade,
    day_of_week           varchar(10),
    start_time            time,
    end_time              time,
    slot_duration_minutes integer default 15,
    consultation_type     VARCHAR(20) NOT NULL DEFAULT 'Offline',
    consultation_fee      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    is_active             boolean default true
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);

create table if not exists appointments
(
    id           serial primary key,
    patient_id   integer references users on delete cascade,
    doctor_id    integer references users on delete cascade,
    slot_id      integer references appointment_slots on delete cascade,
    status       varchar(20) default 'accepted',
    type         varchar(20) ,
    created_at   timestamp   default now()
);

CREATE TABLE if not exists chat_rooms (
    id SERIAL PRIMARY KEY,
    patient_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    doctor_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    appointment_id INT REFERENCES appointments(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_patient_doctor UNIQUE (patient_id, doctor_id)
);

create table if not exists chat_messages
(
    id        serial primary key,
    room_id   integer references chat_rooms on delete cascade,
    sender_id integer references users,
    message   text    not null,
    is_read   boolean default false,
    sent_at   timestamp default now()
);

create table if not exists doctor_schedule_settings (
    id           serial primary key,
    user_id      integer references users on delete cascade unique,
    window_days  integer default 30,
    booking_mode varchar(20) default 'auto',
    created_at   timestamp default now()
);

create table if not exists appointment_slots (
    id               serial primary key,
    doctor_id        integer references users on delete cascade,
    date             date not null,
    start_time       time not null,
    end_time         time not null,
    consultation_fee numeric(10,2) not null,  -- ← copied from slot settings
    status           varchar(20) default 'available',
    appointment_id   integer references appointments,
    created_at       timestamp default now(),

    unique(doctor_id, date, start_time)
);

CREATE TABLE if not exists doctor_slot_settings (
    id                    SERIAL PRIMARY KEY,
    user_id               INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week           VARCHAR(20) NOT NULL,
    start_time            TIME NOT NULL,
    end_time              TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL,
    consultation_fee      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    consultation_type     VARCHAR(20) NOT NULL DEFAULT 'Offline',
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);