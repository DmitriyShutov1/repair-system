CREATE TABLE financial_fact (
    id BIGSERIAL PRIMARY KEY,
    master_id BIGINT,
    branch_id BIGINT NOT NULL,
    order_id BIGINT,
    event_id UUID UNIQUE,        
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    event_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_financial_fact_master_date 
ON financial_fact(master_id, event_date);

CREATE INDEX idx_financial_fact_type 
ON financial_fact(type);


CREATE TABLE master_daily_stats (
    id BIGSERIAL PRIMARY KEY,
    master_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    order_count INT DEFAULT 0,
    cancelled_orders_count INT DEFAULT 0,
    returned_orders_count INT DEFAULT 0,
    total_income NUMERIC(12,2) DEFAULT 0
);

CREATE UNIQUE INDEX uniq_master_day 
ON master_daily_stats(master_id, stat_date);


CREATE TABLE company_daily_stats (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    total_orders INT DEFAULT 0,
    total_income NUMERIC(12,2) DEFAULT 0,
    total_expenses NUMERIC(12,2) DEFAULT 0,
    total_returns INT DEFAULT 0
);

CREATE UNIQUE INDEX uniq_company_day 
ON company_daily_stats(stat_date, branch_id);
