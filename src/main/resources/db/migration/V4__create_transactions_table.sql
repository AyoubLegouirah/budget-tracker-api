CREATE TABLE transactions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount      NUMERIC(15, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    note        TEXT,
    type        VARCHAR(10) NOT NULL,
    date        DATE NOT NULL,
    account_id  UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);