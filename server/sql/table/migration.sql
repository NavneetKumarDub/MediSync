CREATE TABLE IF NOT EXISTS users
(
    id         SERIAL PRIMARY KEY,
    phone      VARCHAR(10)  NOT NULL UNIQUE,
    name       VARCHAR(100),
    role       VARCHAR(10),
    status     VARCHAR(20)  DEFAULT 'active',
    created_at TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS patient_personal
(
    id                SERIAL PRIMARY KEY,
    user_id           INTEGER REFERENCES users ON DELETE CASCADE,
    email             VARCHAR(100),
    gender            VARCHAR(10),
    dob               VARCHAR(20),
    blood_group       VARCHAR(5),
    marital_status    VARCHAR(20),
    height            VARCHAR(10),
    weight            VARCHAR(10),
    emergency_contact VARCHAR(10),
    profile_photo     TEXT
);

CREATE TABLE IF NOT EXISTS patient_medical
(
    id                  SERIAL PRIMARY KEY,
    user_id             INTEGER REFERENCES users ON DELETE CASCADE,
    allergies           TEXT,
    current_medications TEXT,
    past_medications    TEXT,
    chronic_diseases    TEXT,
    injuries            TEXT,
    surgeries           TEXT
);

CREATE TABLE IF NOT EXISTS patient_lifestyle
(
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER UNIQUE REFERENCES users ON DELETE CASCADE,
    smoking         VARCHAR(50),
    alcohol         VARCHAR(50),
    activity_level  VARCHAR(50),
    food_preference VARCHAR(50),
    occupation      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS doctor_personal
(
    id             SERIAL PRIMARY KEY,
    user_id        INTEGER REFERENCES users ON DELETE CASCADE,
    email          VARCHAR(100),
    gender         VARCHAR(10),
    dob            VARCHAR(20),
    marital_status VARCHAR(20),
    profile_photo  TEXT,
    about          TEXT
);

CREATE TABLE IF NOT EXISTS doctor_professional
(
    id                SERIAL PRIMARY KEY,
    user_id           INTEGER REFERENCES users ON DELETE CASCADE,
    license_number    VARCHAR(50) UNIQUE NOT NULL,
    speciality        VARCHAR(100),
    sub_speciality    VARCHAR(100),
    qualification     VARCHAR(200),
    experience_years  INTEGER,
    languages         TEXT,
    consultation_fee  NUMERIC(10, 2),
    consultation_type VARCHAR(20) DEFAULT 'both'
);

CREATE TABLE IF NOT EXISTS doctor_clinic
(
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES users ON DELETE CASCADE,
    clinic_name VARCHAR(200),
    address     TEXT,
    city        VARCHAR(100),
    pincode     VARCHAR(10),
    lat         NUMERIC(10, 7),
    lng         NUMERIC(10, 7)
);

CREATE TABLE IF NOT EXISTS doctor_availability
(
    id                    SERIAL PRIMARY KEY,
    user_id               INTEGER REFERENCES users ON DELETE CASCADE,
    day_of_week           VARCHAR(10),
    start_time            TIME,
    end_time              TIME,
    slot_duration_minutes INTEGER       DEFAULT 15,
    consultation_type     VARCHAR(20)   NOT NULL DEFAULT 'Offline',
    consultation_fee      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    is_active             BOOLEAN       DEFAULT TRUE,
    created_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_doctor_availability_user_id
    ON doctor_availability (user_id);

CREATE INDEX IF NOT EXISTS idx_doctor_availability_user_day
    ON doctor_availability (user_id, day_of_week);

CREATE TABLE IF NOT EXISTS doctor_schedule_settings
(
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER REFERENCES users ON DELETE CASCADE UNIQUE,
    window_days  INTEGER     DEFAULT 30,
    booking_mode VARCHAR(20) DEFAULT 'auto',
    created_at   TIMESTAMP   DEFAULT NOW()
);  

CREATE TABLE IF NOT EXISTS appointment_slots
(
    id                    SERIAL PRIMARY KEY,
    doctor_id             INTEGER REFERENCES users ON DELETE CASCADE,
    date                  DATE           NOT NULL,
    start_time            TIME           NOT NULL,
    end_time              TIME           NOT NULL,
    consultation_fee      NUMERIC(10, 2) NOT NULL,
    slot_duration_minutes INTEGER,
    is_active             BOOLEAN        DEFAULT TRUE,
    consultation_type     VARCHAR(20)    DEFAULT 'Offline',
    status                VARCHAR(20)    DEFAULT 'available',
    appointment_id        INTEGER REFERENCES appointments,
    created_at            TIMESTAMP      DEFAULT NOW(),
    UNIQUE (doctor_id, date, start_time)
);

CREATE TABLE IF NOT EXISTS appointments
(
    id         SERIAL PRIMARY KEY,
    patient_id INTEGER REFERENCES users ON DELETE CASCADE,
    doctor_id  INTEGER REFERENCES users ON DELETE CASCADE,
    slot_id    INTEGER REFERENCES appointment_slots ON DELETE CASCADE,
    status     VARCHAR(20) DEFAULT 'accepted',
    type       VARCHAR(20),
    created_at TIMESTAMP   DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS chat_rooms
(
    id             SERIAL PRIMARY KEY,
    patient_id     INTEGER   NOT NULL REFERENCES users ON DELETE CASCADE,
    doctor_id      INTEGER   NOT NULL REFERENCES users ON DELETE CASCADE,
    appointment_id INTEGER REFERENCES appointments ON DELETE SET NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_patient_doctor UNIQUE (patient_id, doctor_id)
);

CREATE TABLE IF NOT EXISTS chat_messages
(
    id        SERIAL PRIMARY KEY,
    room_id   INTEGER REFERENCES chat_rooms ON DELETE CASCADE,
    sender_id INTEGER REFERENCES users,
    message   TEXT    NOT NULL,
    is_read   BOOLEAN   DEFAULT FALSE,
    sent_at   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS doctor_slot_settings
(
    id                    SERIAL PRIMARY KEY,
    user_id               INTEGER        NOT NULL REFERENCES users ON DELETE CASCADE,
    day_of_week           VARCHAR(20)    NOT NULL,
    start_time            TIME           NOT NULL,
    end_time              TIME           NOT NULL,
    slot_duration_minutes INTEGER        NOT NULL,
    consultation_fee      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    consultation_type     VARCHAR(20)    NOT NULL DEFAULT 'Offline',
    is_active             BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);