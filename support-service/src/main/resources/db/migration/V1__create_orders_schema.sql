CREATE TABLE support_requests (
    id BIGSERIAL PRIMARY KEY,

    support_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,

    master_id BIGINT,
    completed_by_master_id BIGINT,

    description TEXT,
    device_serial VARCHAR(100),       
    device_model  VARCHAR(255),       

    status VARCHAR(50) NOT NULL,

    cost NUMERIC(12,2),
    master_cost NUMERIC(12,2),
    refund_cost NUMERIC(12,2),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_support_requests_support_id
    ON support_requests (support_id);

CREATE INDEX idx_support_requests_order_id
    ON support_requests (order_id);

CREATE INDEX idx_support_requests_master_id
    ON support_requests (master_id);


CREATE TABLE problem_items (
    id BIGSERIAL PRIMARY KEY,

    support_request_id BIGINT NOT NULL,

    item_type VARCHAR(50) NOT NULL,

    name VARCHAR(255) NOT NULL,

    category VARCHAR(100),

    sell_price NUMERIC(12,2),

    quantity INTEGER NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_problem_items_support_request
        FOREIGN KEY (support_request_id)
        REFERENCES support_requests(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_problem_items_support_request
    ON problem_items (support_request_id);
    
    

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

CREATE INDEX idx_outbox_processed ON outbox_event(processed);