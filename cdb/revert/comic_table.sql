-- Revert comic_table

BEGIN;

DROP TABLE cg.comic;

COMMIT;
