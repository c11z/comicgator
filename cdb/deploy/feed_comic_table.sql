-- Deploy cdb:feed_comic_table to pg
-- requires: feed_table
-- requires: comic_table

BEGIN;

CREATE TABLE cg.feed_comic (
feed_id VARCHAR(24) NOT NULL REFERENCES cg.feed(id),
comic_id VARCHAR(24) NOT NULL REFERENCES cg.comic(id),
is_latest BOOLEAN NOT NULL DEFAULT false,
is_replay BOOLEAN NOT NULL DEFAULT false,
mark INT NOT NULL DEFAULT 0,
step INT NOT NULL DEFAULT 0,
interlude INTERVAL NOT NULL DEFAULT '1 day',
start_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
next_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
updated_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
PRIMARY KEY (feed_id, comic_id));

CREATE INDEX feed_comic_ix_updated_at ON cg.feed_comic (updated_at);
CREATE INDEX feed_comic_ix_next_at ON cg.feed_comic (next_at);

GRANT SELECT, UPDATE, INSERT ON cg.feed_comic TO mrcg;

COMMIT;
