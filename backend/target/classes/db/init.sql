CREATE TABLE IF NOT EXISTS portfolios (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    units_owned DECIMAL NOT NULL,
    fund_composition JSONB
);

CREATE TABLE IF NOT EXISTS investments (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT REFERENCES portfolios(id),
    symbol VARCHAR(10) NOT NULL,
    quantity DECIMAL NOT NULL,
    CONSTRAINT fk_portfolio FOREIGN KEY(portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS market_data (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    price DECIMAL NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_market_data_symbol ON market_data(symbol);
CREATE INDEX idx_investments_portfolio ON investments(portfolio_id); 