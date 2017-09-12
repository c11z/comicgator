-- Verify update_timestamp_trigger

BEGIN;

SELECT has_function_privilege('cg.update_timestamp()', 'execute');

SELECT 1/count(*) FROM pg_trigger WHERE tgname = 'comic_tg_update_timestamp';
SELECT 1/count(*) FROM pg_trigger WHERE tgname = 'geek_tg_update_timestamp';
SELECT 1/count(*) FROM pg_trigger WHERE tgname = 'strip_tg_update_timestamp';
SELECT 1/count(*) FROM pg_trigger WHERE tgname = 'comic_geek_tg_update_timestamp';
SELECT 1/count(*) FROM pg_trigger WHERE tgname = 'strip_geek_tg_update_timestamp';
SELECT 1/count(*) FROM pg_trigger WHERE tgname = 'feed_tg_update_timestamp';
SELECT 1/count(*) FROM pg_trigger WHERE tgname = 'feed_comic_tg_update_timestamp';
SELECT 1/count(*) FROM pg_trigger WHERE tgname = 'feed_strip_tg_update_timestamp';


ROLLBACK;
