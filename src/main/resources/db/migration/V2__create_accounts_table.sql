CREATE TABLE accounts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    balance     NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    currency    VARCHAR(3) NOT NULL DEFAULT 'EUR',
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);