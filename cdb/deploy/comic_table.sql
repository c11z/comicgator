-- Deploy comic_table
-- requires: schema
-- requires: generate_object_id

BEGIN;

CREATE TABLE cg.comic (
id VARCHAR(24) NOT NULL DEFAULT cg.generate_object_id() PRIMARY KEY,
hostname TEXT NOT NULL,
title TEXT NOT NULL,
creator TEXT NOT NULL,
is_advertised BOOLEAN NOT NULL,
first_url TEXT NOT NULL,
patreon_url TEXT DEFAULT NULL,
store_url TEXT DEFAULT NULL,
banner_image TEXT NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
updated_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'));

CREATE INDEX comic_ix_updated_at ON cg.comic (updated_at);

GRANT SELECT, UPDATE, INSERT ON cg.comic TO mrcg;

COMMIT;
