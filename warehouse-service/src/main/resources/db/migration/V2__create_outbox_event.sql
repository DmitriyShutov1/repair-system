CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,

    event_id UUID NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,

    payload TEXT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP,

    retry_count INT DEFAULT 0
);