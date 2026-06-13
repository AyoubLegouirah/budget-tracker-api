-- Replace the basic UNIQUE constraint with a partial index that explicitly
-- allows multiple NULL tink_id (manual transactions) while still
-- blocking duplicate Tink imports.
ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_tink_id_key;
CREATE UNIQUE INDEX transactions_tink_id_unique
    ON transactions (tink_id)
    WHERE tink_id IS NOT NULL;
