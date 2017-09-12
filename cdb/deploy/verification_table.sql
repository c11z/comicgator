-- Deploy cdb:verification_table to pg
-- requires: geek_table

BEGIN;

CREATE TABLE cg.verification (
email TEXT NOT NULL,
code VARCHAR(6) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'));

CREATE UNIQUE INDEX verification_uq_email ON cg.verification (email);
CREATE INDEX verification_ix_created_at ON cg.verification (created_at);

GRANT SELECT, UPDATE, INSERT ON cg.verification TO mrcg;

COMMIT;
