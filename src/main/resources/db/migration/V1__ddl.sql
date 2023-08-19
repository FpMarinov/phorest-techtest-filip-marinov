CREATE TABLE client
(
    id                           UUID PRIMARY KEY,
    first_name                   TEXT NOT NULL,
    last_name                    TEXT NOT NULL,
    email                        TEXT NOT NULL,
    phone                        TEXT NOT NULL,
    gender                       TEXT NOT NULL,
    banned                       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE appointment
(
    id                           UUID PRIMARY KEY,
    start_time                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time                     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    client_id                    UUID NOT NULL REFERENCES client (id),
    created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE purchase
(
    id                           UUID PRIMARY KEY,
    name                         TEXT NOT NULL,
    price                        DECIMAL(19, 2) NOT NULL,
    loyalty_points               INTEGER NOT NULL,
    appointment_id               UUID NOT NULL REFERENCES appointment (id),
    created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE service
(
    id                           UUID PRIMARY KEY,
    name                         TEXT NOT NULL,
    price                        DECIMAL(19, 2) NOT NULL,
    loyalty_points               INTEGER NOT NULL,
    appointment_id               UUID NOT NULL REFERENCES appointment (id),
    created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
