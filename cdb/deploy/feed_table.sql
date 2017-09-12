-- Deploy cdb:feed_table to pg
-- requires: geek_table

BEGIN;

CREATE TABLE cg.feed (
id VARCHAR(24) NOT NULL DEFAULT cg.generate_object_id() PRIMARY KEY,
geek_id VARCHAR(24) NOT NULL REFERENCES cg.geek(id),
name TEXT NOT NULL DEFAULT '',
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
updated_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'));

CREATE INDEX feed_ix_geek_id ON cg.feed (geek_id);
CREATE INDEX feed_ix_updated_at ON cg.feed (updated_at);

GRANT SELECT, UPDATE, INSERT ON cg.feed TO mrcg;

COMMIT;
