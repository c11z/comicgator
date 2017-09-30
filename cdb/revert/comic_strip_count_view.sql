-- Revert cdb:comic_strip_count_view from pg

BEGIN;

DROP MATERIALIZED VIEW cg.comic_strip_count;

COMMIT;
