-- Verify cdb:session_geek_table on pg

BEGIN;

SELECT geek_id, token, created_at
FROM cg.session_geek
WHERE FALSE;

SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'session_geek_ix_geek_id';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'session_geek_ix_created_at';

SELECT has_table_privilege('mrcg', 'cg.session_geek', 'select');
SELECT has_table_privilege('mrcg', 'cg.session_geek', 'update');
SELECT has_table_privilege('mrcg', 'cg.session_geek', 'insert');

ROLLBACK;
