-- Revert cdb:session_geek_table from pg

BEGIN;

DROP TABLE cg.session_geek;

COMMIT;
