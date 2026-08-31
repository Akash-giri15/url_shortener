-- First, a sanity check worth running once by hand before this migration:
-- SELECT short_code, COUNT(*) FROM urls GROUP BY short_code HAVING COUNT(*) > 1;
-- If that returns any rows, a UNIQUE index will fail to create until they're resolved.

CREATE UNIQUE INDEX idx_short_code ON urls (short_code);