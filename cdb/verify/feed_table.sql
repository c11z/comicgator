-- Verify cdb:feed_table on pg

BEGIN;

SELECT id,
	   geek_id,
       name,
 	   created_at,
 	   updated_at
 FROM cg.feed
 WHERE FALSE;

SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'feed_ix_geek_id';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'feed_ix_updated_at';

SELECT has_table_privilege('mrcg', 'cg.feed', 'select');
SELECT has_table_privilege('mrcg', 'cg.feed', 'update');
SELECT has_table_privilege('mrcg', 'cg.feed', 'insert');

ROLLBACK;
