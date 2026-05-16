
CREATE TABLE branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(500) NOT NULL,
    phone VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_branch_name ON branches(name);

CREATE TABLE user_accounts (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    branch_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE INDEX idx_user_email ON user_accounts(email);
CREATE INDEX idx_user_phone ON user_accounts(phone);
CREATE INDEX idx_user_branch ON user_accounts(branch_id);
CREATE INDEX idx_user_role ON user_accounts(role);
CREATE INDEX idx_user_status ON user_accounts(status);

ALTER TABLE user_accounts
    ADD CONSTRAINT fk_user_branch
    FOREIGN KEY (branch_id)
    REFERENCES branches(id)
    ON DELETE SET NULL;

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(255),
    user_agent TEXT,
    ip_address VARCHAR(64),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_expiry ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_token_user_device_revoked 
    ON refresh_tokens(user_id, device_id, revoked);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_user
    FOREIGN KEY (user_id)
    REFERENCES user_accounts(id)
    ON DELETE CASCADE;

    
-- Пароль: adminadmin (bcrypt hash)
INSERT INTO branches (name, address, phone)
VALUES ('Main Branch', 'г. Москва, ул. Ленина, д. 1', '+7 (495) 123-45-67')
ON CONFLICT (name) DO NOTHING;

INSERT INTO user_accounts (
    email,
    phone,
    password_hash,
    role,
    status,
    branch_id,
    created_at
)
SELECT
    'admin@admin.admin',
    '89123746289',
    '$2a$10$SD/wLhNgEiApONo31ubSAu7vPYeGjr.Xd3lJGe5xhTDCeeTpETkMm',
    'ADMIN',
    'ACTIVE',
    id,
    NOW()
FROM branches
WHERE name = 'Main Branch'
ON CONFLICT (email) DO NOTHING;