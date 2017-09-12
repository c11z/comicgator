-- Deploy geek_table
-- requires: schema
-- requires: generate_object_id

BEGIN;

CREATE TABLE cg.geek (
id VARCHAR(24) NOT NULL DEFAULT cg.generate_object_id() PRIMARY KEY,
email TEXT NOT NULL,
is_receptive BOOLEAN NOT NULL DEFAULT FALSE,
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
updated_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'));

CREATE UNIQUE INDEX geek_uq_email ON cg.geek (email);
CREATE INDEX geek_ix_updated_at ON cg.geek (updated_at);

GRANT SELECT, UPDATE, INSERT ON cg.geek TO mrcg;

COMMIT;
