-- Verify appschema

BEGIN;

SELECT pg_catalog.has_schema_privilege('cg', 'usage');

ROLLBACK;
