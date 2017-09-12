-- Verify geek_table

BEGIN;

SELECT id, email, is_receptive, created_at, updated_at
FROM cg.geek
WHERE FALSE;

SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'geek_uq_email';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'geek_ix_updated_at';

SELECT has_table_privilege('mrcg', 'cg.geek', 'select');
SELECT has_table_privilege('mrcg', 'cg.geek', 'update');
SELECT has_table_privilege('mrcg', 'cg.geek', 'insert');

ROLLBACK;
