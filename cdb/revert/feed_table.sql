-- Revert cdb:feed_table from pg

BEGIN;

DROP TABLE cg.feed;

COMMIT;
