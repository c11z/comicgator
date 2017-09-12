-- Revert cdb:feed_strip_table from pg

BEGIN;

DROP TABLE cg.feed_strip;

COMMIT;
