-- Core tables for forms and service requests.
-- Written to run on PostgreSQL (production) and H2 in PostgreSQL mode (tests).

CREATE TABLE service_request (
    id              UUID PRIMARY KEY,
    tracking_number VARCHAR(20)   NOT NULL UNIQUE,
    category        VARCHAR(40)   NOT NULL,
    description     VARCHAR(4000) NOT NULL,
    location        VARCHAR(300),
    contact_name    VARCHAR(100),
    contact_email   VARCHAR(254),
    contact_phone   VARCHAR(30),
    status          VARCHAR(20)   NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    version         BIGINT        NOT NULL
);

CREATE INDEX idx_service_request_status_created ON service_request (status, created_at);

CREATE TABLE form_submission (
    id               UUID PRIMARY KEY,
    reference_number VARCHAR(20)    NOT NULL UNIQUE,
    form_key         VARCHAR(60)    NOT NULL,
    payload          VARCHAR(20000) NOT NULL,
    submitted_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_form_submission_key_time ON form_submission (form_key, submitted_at);

-- Append-only. Grant the application role INSERT and SELECT only on this table in production.
CREATE TABLE audit_event (
    id           UUID PRIMARY KEY,
    occurred_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    actor        VARCHAR(200)  NOT NULL,
    action       VARCHAR(60)   NOT NULL,
    subject_type VARCHAR(60)   NOT NULL,
    subject_id   VARCHAR(60)   NOT NULL,
    detail       VARCHAR(2000)
);

CREATE INDEX idx_audit_event_subject ON audit_event (subject_type, subject_id);
