-- Verify comic_geek_table

BEGIN;

SELECT geek_id, comic_id, is_viewed, is_starred, created_at, updated_at
 FROM cg.comic_geek
 WHERE FALSE;

SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'comic_geek_ix_comic_id';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'comic_geek_ix_geek_id';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'comic_geek_ix_updated_at';

SELECT has_table_privilege('mrcg', 'cg.comic_geek', 'select');
SELECT has_table_privilege('mrcg', 'cg.comic_geek', 'update');
SELECT has_table_privilege('mrcg', 'cg.comic_geek', 'insert');

ROLLBACK;
