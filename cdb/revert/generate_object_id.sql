-- Revert generate_object_id

BEGIN;

DROP FUNCTION cg.generate_object_id();

COMMIT;