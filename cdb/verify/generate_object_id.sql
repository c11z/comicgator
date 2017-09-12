-- Verify generate_object_id

BEGIN;

SELECT has_function_privilege('cg.generate_object_id()', 'execute');

ROLLBACK;