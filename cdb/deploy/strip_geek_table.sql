-- Deploy strip_geek_table
-- requires: schema
-- requires: strip_table
-- requires: geek_table

BEGIN;

CREATE TABLE cg.strip_geek (
geek_id VARCHAR(24) NOT NULL REFERENCES cg.geek(id),
strip_id VARCHAR(24) NOT NULL REFERENCES cg.strip(id),
is_viewed BOOLEAN DEFAULT FALSE,
is_starred BOOLEAN DEFAULT FALSE,
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
updated_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
PRIMARY KEY (geek_id, strip_id));

CREATE INDEX strip_geek_ix_geek_id ON cg.strip_geek (geek_id);
CREATE INDEX strip_geek_ix_strip_id ON cg.strip_geek (strip_id);
CREATE INDEX strip_geek_ix_updated_at ON cg.strip_geek (updated_at);

GRANT SELECT, UPDATE, INSERT ON cg.strip_geek TO mrcg;

COMMIT;
