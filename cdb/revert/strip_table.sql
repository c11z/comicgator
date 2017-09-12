-- Revert strip_table

BEGIN;

DROP TABLE cg.strip;

COMMIT;
