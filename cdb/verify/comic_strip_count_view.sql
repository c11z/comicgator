-- Verify cdb:comic_strip_count_view on pg

BEGIN;

SELECT comic_id, strip_count
FROM cg.comic_strip_count WHERE FALSE;

ROLLBACK;
