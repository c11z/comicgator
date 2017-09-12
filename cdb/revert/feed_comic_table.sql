-- Revert cdb:feed_comic_table from pg

BEGIN;

DROP TABLE cg.feed_comic;

COMMIT;
