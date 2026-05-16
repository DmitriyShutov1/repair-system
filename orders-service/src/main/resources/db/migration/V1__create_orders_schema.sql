
CREATE TABLE orders (
    order_id        BIGSERIAL PRIMARY KEY,
    client_id       BIGINT NOT NULL,
    master_id       BIGINT,
    branch_id       BIGINT,
    device_serial     VARCHAR(100) NOT NULL, 
    device_model      VARCHAR(255) NOT NULL, 
    warranty_id     BIGINT,
    status          VARCHAR(50) NOT NULL,
    diagnostic_result TEXT,
    client_approved BOOLEAN DEFAULT FALSE,
    pickup_code     VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    completed_at    TIMESTAMP
);

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



CREATE INDEX idx_orders_client_id
    ON orders(client_id);

CREATE INDEX idx_orders_master_id
    ON orders(master_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_created_at
    ON orders(created_at);

CREATE INDEX idx_orders_device_serial
    ON orders(device_serial);


CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);

CREATE INDEX idx_order_items_item_id
    ON order_items(item_id);


CREATE INDEX idx_status_history_order_id
    ON order_status_history(order_id);

CREATE INDEX idx_status_history_changed_at
    ON order_status_history(changed_at);
    
CREATE INDEX idx_outbox_processed 
	ON outbox_event(processed);
	
	
	
CREATE TABLE tests (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tests_active
    ON tests(active);

    
    
CREATE TABLE test_sessions (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    session_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_test_sessions_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_test_sessions_order_id
    ON test_sessions(order_id);

CREATE INDEX idx_test_sessions_session_at
    ON test_sessions(session_at);

    
    
CREATE TABLE test_session_steps (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT NOT NULL,
    test_id         BIGINT NOT NULL,
    passed          BOOLEAN NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_steps_session
        FOREIGN KEY (session_id)
        REFERENCES test_sessions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_steps_test
        FOREIGN KEY (test_id)
        REFERENCES tests(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_steps_session_id
    ON test_session_steps(session_id);

CREATE INDEX idx_steps_test_id
    ON test_session_steps(test_id);
    
INSERT INTO tests (name, description, active) VALUES
('Стресс-тест', 'Прогнать AIDA64 или OCCT в течение 10 минут. Проверить стабильность системы под нагрузкой, отсутствие перегрева и троттлинга.', TRUE),
('Тест экрана', 'Проверить матрицу на битые пиксели, засветы, неравномерность подсветки. Использовать Eizo Monitor Test или аналоги.', TRUE),
('Тест системы охлаждения', 'Проверить температуры процессора и видеокарты в простое и под нагрузкой. Убедиться, что вентиляторы работают корректно на всех режимах.', TRUE),
('Тест петель', 'Проверить плавность хода крышки ноутбука. Отсутствие люфта, скрипа, самопроизвольного закрывания. Проверить фиксацию в крайних положениях.', TRUE),
('Тест оперативной памяти', 'Прогнать MemTest86 не менее одного полного прохода. Убедиться в отсутствии ошибок.', TRUE),
('Тест видеочипа', 'Прогнать FurMark или аналогичный бенчмарк. Проверить отсутствие артефактов, перегрева, отвала чипа.', TRUE);
