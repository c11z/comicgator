-- Deploy cdb:comic_strip_count_view to pg
-- requires: strip_table

BEGIN;

CREATE MATERIALIZED VIEW cg.comic_strip_count AS
SELECT
    s.comic_id,
    count(s.id) AS strip_count
FROM cg.strip s
GROUP BY s.comic_id;

COMMIT;
