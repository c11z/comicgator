-- Verify comic_table

BEGIN;

SELECT id,
	   hostname, 
	   title, 
	   creator,
	   first_url,
 	   created_at,
 	   updated_at
 FROM cg.comic
 WHERE FALSE;

SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'comic_pkey';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'comic_ix_updated_at';

SELECT has_table_privilege('mrcg', 'cg.comic', 'select');
SELECT has_table_privilege('mrcg', 'cg.comic', 'update');
SELECT has_table_privilege('mrcg', 'cg.comic', 'insert');

ROLLBACK;
