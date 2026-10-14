CREATE TABLE wallet_transactions (
     transaction_id UUID PRIMARY KEY,

     wallet_id VARCHAR(100) NOT NULL
         REFERENCES wallets(wallet_id),

     event_id UUID NOT NULL UNIQUE,

     transaction_type VARCHAR(20) NOT NULL
         CHECK (transaction_type IN ('DEBIT', 'CREDIT')),

     amount NUMERIC(19, 2) NOT NULL
         CHECK (amount > 0),

     balance_after NUMERIC(19, 2) NOT NULL
         CHECK (balance_after >= 0),

     created_at TIMESTAMPTZ NOT NULL
         DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallet_transactions_wallet_created
    ON wallet_transactions(wallet_id, created_at DESC);