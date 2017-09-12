-- Verify cdb:feed_comic_table on pg

BEGIN;

SELECT feed_id,
	   comic_id,
       is_latest,
       is_replay,
       mark,
       step,
       interlude,
       start_at,
       next_at,
 	   created_at,
 	   updated_at
 FROM cg.feed_comic
 WHERE FALSE;

SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'feed_comic_ix_updated_at';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'feed_comic_ix_next_at';

SELECT has_table_privilege('mrcg', 'cg.feed_comic', 'select');
SELECT has_table_privilege('mrcg', 'cg.feed_comic', 'update');
SELECT has_table_privilege('mrcg', 'cg.feed_comic', 'insert');

ROLLBACK;
