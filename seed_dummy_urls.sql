INSERT INTO urls (short_code, original_url, created_at)
SELECT
    'seed' || gs::text,                         -- fake codes, won't collide with your real Base62 ones
    'https://example.com/seed/' || gs::text,
    now()
FROM generate_series(1, 100000) AS gs;

ANALYZE urls;   -- refresh Postgres's internal statistics so the planner's cost estimates are accurate