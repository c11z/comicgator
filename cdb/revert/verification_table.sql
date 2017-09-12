-- Revert cdb:verification_table from pg

BEGIN;

DROP TABLE cg.verification;

COMMIT;
