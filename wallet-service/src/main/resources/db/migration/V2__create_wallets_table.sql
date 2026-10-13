CREATE TABLE wallets (
    wallet_id VARCHAR(100) PRIMARY KEY,

    balance NUMERIC(19, 2) NOT NULL CHECK (balance >= 0),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO wallets (wallet_id, balance)
VALUES ('wallet-101', 10000.00);