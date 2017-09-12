-- Verify strip_geek_table

BEGIN;

SELECT geek_id, strip_id, is_viewed, is_starred, created_at, updated_at
 FROM cg.strip_geek
 WHERE FALSE;

SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'strip_geek_ix_strip_id';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'strip_geek_ix_geek_id';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'strip_geek_ix_updated_at';

SELECT has_table_privilege('mrcg', 'cg.strip_geek', 'select');
SELECT has_table_privilege('mrcg', 'cg.strip_geek', 'update');
SELECT has_table_privilege('mrcg', 'cg.strip_geek', 'insert');

ROLLBACK;
