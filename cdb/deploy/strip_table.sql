-- Deploy strip_table
-- requires: schema
-- requires: generate_object_id
-- requires: comic_table

BEGIN;

CREATE TABLE cg.strip (
id VARCHAR(24) NOT NULL DEFAULT cg.generate_object_id() PRIMARY KEY,
comic_id VARCHAR(24) NOT NULL REFERENCES cg.comic(id),
checksum VARCHAR(32) NOT NULL,
number INTEGER NOT NULL,
url TEXT NOT NULL,
image_urls TEXT[] NOT NULL,
thumbnail_image_url TEXT NOT NULL,
title TEXT DEFAULT NULL,
image_alt TEXT DEFAULT NULL,
image_title TEXT DEFAULT NULL,
bonus_image_url TEXT DEFAULT NULL,
is_special BOOLEAN DEFAULT FALSE,
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
updated_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'));

CREATE UNIQUE INDEX strip_uq_comic_id_number ON cg.strip (comic_id, number);
CREATE INDEX strip_ix_updated_at ON cg.strip (updated_at);

GRANT SELECT, UPDATE, INSERT ON cg.strip TO mrcg;

COMMIT;
