-- Revert strip_geek_table

BEGIN;

DROP TABLE cg.strip_geek;

COMMIT;
