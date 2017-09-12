-- Deploy comic_geek_table
-- requires: schema
-- requires: comic_table
-- requires: geek_table

BEGIN;

CREATE TABLE cg.comic_geek (
geek_id VARCHAR(24) NOT NULL REFERENCES cg.geek(id),
comic_id VARCHAR(24) NOT NULL REFERENCES cg.comic(id),
is_viewed BOOLEAN DEFAULT FALSE,
is_starred BOOLEAN DEFAULT FALSE,
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
updated_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
PRIMARY KEY (geek_id, comic_id));

CREATE INDEX comic_geek_ix_geek_id ON cg.comic_geek (geek_id);
CREATE INDEX comic_geek_ix_comic_id ON cg.comic_geek (comic_id);
CREATE INDEX comic_geek_ix_updated_at ON cg.comic_geek (updated_at);

GRANT SELECT, UPDATE, INSERT ON cg.comic_geek TO mrcg;

COMMIT;
