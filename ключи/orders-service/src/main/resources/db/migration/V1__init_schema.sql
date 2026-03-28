-- ======================================
-- EXTENSIONS
-- ======================================
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ======================================
-- ENUM TYPES
-- ======================================

CREATE TYPE part_category_enum AS ENUM (
    'SCREEN',
    'BATTERY',
    'MOTHERBOARD',
    'CPU',
    'GPU',
    'COOLING_SYSTEM',
    'KEYBOARD',
    'RAM',
    'SSD',
    'MICROELEMENT'
);

CREATE TYPE service_category_enum AS ENUM (
    'SOFTWARE',
    'MAINTENANCE',
    'SOLDERING',
    'DIAGNOSTICS',
    'CLEANING',
    'UPGRADE'
);

CREATE TYPE movement_type_enum AS ENUM (
    'IN',
    'OUT',
    'WRITE_OFF',
    'RETURN'
);

-- ======================================
-- LAPTOP MODEL
-- ======================================

CREATE TABLE laptop_model (
    id                  BIGSERIAL PRIMARY KEY,
    brand               VARCHAR(100) NOT NULL,
    model_name          VARCHAR(150) NOT NULL,
    model_series_code   VARCHAR(100),
    release_year        INTEGER,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_laptop UNIQUE (brand, model_name)
);

CREATE INDEX idx_laptop_brand ON laptop_model(brand);

-- ======================================
-- PART
-- ======================================

CREATE TABLE part (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    article_number  VARCHAR(100) NOT NULL UNIQUE,
    category        VARCHAR(100) NOT NULL,
--    category        part_category_enum NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_part_category ON part(category);

-- trigram index for fast search
CREATE INDEX idx_part_name_trgm
    ON part USING GIN (name gin_trgm_ops);

-- ======================================
-- SERVICE
-- ======================================

CREATE TABLE service (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    service_code    VARCHAR(100) NOT NULL UNIQUE,
    category        VARCHAR(100) NOT NULL,
--    category        service_category_enum NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_service_category ON service(category);

CREATE INDEX idx_service_name_trgm
    ON service USING GIN (name gin_trgm_ops);

-- ======================================
-- LAPTOP-PART COMPATIBILITY (M:N)
-- ======================================

--CREATE TABLE laptop_model_part (
--    laptop_model_id BIGINT NOT NULL,
--    part_id         BIGINT NOT NULL,
--    PRIMARY KEY (laptop_model_id, part_id),
--    CONSTRAINT fk_cmp_laptop
--        FOREIGN KEY (laptop_model_id)
--        REFERENCES laptop_model(id)
--        ON DELETE CASCADE,
--    CONSTRAINT fk_cmp_part
--        FOREIGN KEY (part_id)
--        REFERENCES part(id)
--        ON DELETE CASCADE
--);
--
--CREATE INDEX idx_cmp_part ON laptop_model_part(part_id);

CREATE TABLE laptop_model_part (
    id              BIGSERIAL PRIMARY KEY,
    laptop_model_id BIGINT NOT NULL,
    part_id         BIGINT NOT NULL,

    CONSTRAINT fk_cmp_laptop
        FOREIGN KEY (laptop_model_id)
        REFERENCES laptop_model(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_cmp_part
        FOREIGN KEY (part_id)
        REFERENCES part(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_laptop_part UNIQUE (laptop_model_id, part_id)
);

CREATE INDEX idx_cmp_part ON laptop_model_part(part_id);
CREATE INDEX idx_cmp_laptop ON laptop_model_part(laptop_model_id);

-- ======================================
-- STOCK BALANCE (BY BRANCH)
-- ======================================

--CREATE TABLE stock_balance (
--    part_id     BIGINT NOT NULL,
--    branch_id   BIGINT NOT NULL,
--    quantity    INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
--    version     BIGINT NOT NULL DEFAULT 0,
--    PRIMARY KEY (part_id, branch_id),
--    CONSTRAINT fk_stock_part
--        FOREIGN KEY (part_id)
--        REFERENCES part(id)
--        ON DELETE CASCADE
--);
--
--CREATE INDEX idx_stock_branch ON stock_balance(branch_id);

CREATE TABLE stock_balance (
    id          BIGSERIAL PRIMARY KEY,
    part_id     BIGINT NOT NULL,
    branch_id   BIGINT NOT NULL,
    quantity    INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    version     BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_stock_part
        FOREIGN KEY (part_id)
        REFERENCES part(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_stock_part_branch UNIQUE (part_id, branch_id)
);

CREATE INDEX idx_stock_branch ON stock_balance(branch_id);
CREATE INDEX idx_stock_part ON stock_balance(part_id);

-- ======================================
-- PRICING POLICY (VERSIONED)
-- ======================================

CREATE TABLE pricing_policy (
    id                  BIGSERIAL PRIMARY KEY,
    part_id             BIGINT,
    service_id          BIGINT,
    cost_price          NUMERIC(12,2) NOT NULL CHECK (cost_price >= 0),
    client_price        NUMERIC(12,2) NOT NULL CHECK (client_price >= 0),
    master_percentage   NUMERIC(5,2) CHECK (master_percentage >= 0),
    effective_from      TIMESTAMP NOT NULL,
    effective_to        TIMESTAMP,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_price_part
        FOREIGN KEY (part_id)
        REFERENCES part(id),
    CONSTRAINT fk_price_service
        FOREIGN KEY (service_id)
        REFERENCES service(id),
    CONSTRAINT chk_price_target
        CHECK (
            (part_id IS NOT NULL AND service_id IS NULL)
            OR
            (part_id IS NULL AND service_id IS NOT NULL)
        )
);

CREATE INDEX idx_price_part ON pricing_policy(part_id);
CREATE INDEX idx_price_service ON pricing_policy(service_id);
CREATE INDEX idx_price_effective ON pricing_policy(effective_from, effective_to);
CREATE INDEX idx_price_active_part ON pricing_policy(part_id) WHERE effective_to IS NULL;
--CREATE UNIQUE INDEX uq_active_price_part ON pricing_policy(part_id) WHERE effective_to IS NULL;

-- ======================================
-- STOCK MOVEMENT (AUDIT)
-- ======================================

CREATE TABLE stock_movement (
    id              BIGSERIAL PRIMARY KEY,
    part_id         BIGINT NOT NULL,
    branch_id       BIGINT NOT NULL,
--    movement_type   movement_type_enum NOT NULL,
	movement_type   VARCHAR(55) NOT NULL,
    reason          VARCHAR(255),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    order_id        BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_movement_part
        FOREIGN KEY (part_id)
        REFERENCES part(id)
);

CREATE INDEX idx_movement_part ON stock_movement(part_id);
CREATE INDEX idx_movement_branch ON stock_movement(branch_id);
CREATE INDEX idx_movement_order ON stock_movement(order_id);
CREATE INDEX idx_movement_created ON stock_movement(created_at);

-- ======================================
-- PART WAITING LIST
-- ======================================

CREATE TABLE part_waiting_list (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL,
    part_id             BIGINT NOT NULL,
    branch_id           BIGINT NOT NULL,
    required_quantity   INTEGER NOT NULL CHECK (required_quantity > 0),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    is_closed           BOOLEAN NOT NULL DEFAULT FALSE,
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_wait_part
        FOREIGN KEY (part_id)
        REFERENCES part(id)
);

CREATE INDEX idx_wait_order ON part_waiting_list(order_id);
CREATE INDEX idx_wait_part ON part_waiting_list(part_id);
CREATE INDEX idx_wait_branch ON part_waiting_list(branch_id);