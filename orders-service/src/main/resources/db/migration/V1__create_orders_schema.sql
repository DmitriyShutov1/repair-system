-- =========================
-- ORDERS TABLE
-- =========================

CREATE TABLE orders (
    order_id        BIGSERIAL PRIMARY KEY,
    client_id       BIGINT NOT NULL,
    master_id       BIGINT,
    branch_id       BIGINT,
    warranty_id     BIGINT,
    status          VARCHAR(50) NOT NULL,
    diagnostic_result TEXT,
    client_approved BOOLEAN DEFAULT FALSE,
    pickup_code     VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    completed_at    TIMESTAMP
);


-- =========================
-- ORDER ITEMS
-- =========================

CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    item_type       VARCHAR(20) NOT NULL,
    item_id         BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    article_number  VARCHAR(100),
    category        VARCHAR(100),
    cost_price          NUMERIC(12,2) NOT NULL, 
    sell_price      NUMERIC(12,2) NOT NULL,
    master_percentage   NUMERIC(5,2),   
    quantity        INTEGER NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE
);


-- =========================
-- ORDER STATUS HISTORY
-- =========================

CREATE TABLE order_status_history (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    old_status  VARCHAR(50),
    new_status  VARCHAR(50) NOT NULL,
    changed_by  BIGINT NOT NULL,
    changed_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_status_history_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE
);


-- =========================
-- INDEXES
-- =========================

-- orders
CREATE INDEX idx_orders_client_id
    ON orders(client_id);

CREATE INDEX idx_orders_master_id
    ON orders(master_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_created_at
    ON orders(created_at);


-- order_items
CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);

CREATE INDEX idx_order_items_item_id
    ON order_items(item_id);


-- order_status_history
CREATE INDEX idx_status_history_order_id
    ON order_status_history(order_id);

CREATE INDEX idx_status_history_changed_at
    ON order_status_history(changed_at);