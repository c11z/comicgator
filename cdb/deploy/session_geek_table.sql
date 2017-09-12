-- Deploy cdb:session_geek_table to pg
-- requires: geek_table

BEGIN;

CREATE TABLE cg.session_geek (
geek_id VARCHAR(24) NOT NULL REFERENCES cg.geek(id),
token VARCHAR(32) NOT NULL, -- Using MD5 Bearer Token generator
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'));

CREATE INDEX session_geek_ix_geek_id ON cg.session_geek (geek_id);
CREATE INDEX session_geek_ix_created_at ON cg.session_geek (created_at);

GRANT SELECT, UPDATE, INSERT ON cg.session_geek TO mrcg;

COMMIT;
