CREATE TABLE urls (
                      id           BIGSERIAL PRIMARY KEY,          -- auto-incrementing 64-bit primary key
                      short_code   VARCHAR(10) NOT NULL,            -- no UNIQUE constraint yet -- that's Checkpoint 5, deliberately
                      original_url TEXT NOT NULL,                   -- the long URL being shortened
                      created_at   TIMESTAMP NOT NULL DEFAULT now() -- populated automatically at insert time
);