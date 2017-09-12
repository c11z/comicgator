-- Revert update_timestamp_trigger

BEGIN;

DROP TRIGGER comic_tg_update_timestamp ON cg.comic;
DROP TRIGGER geek_tg_update_timestamp ON cg.geek;
DROP TRIGGER strip_tg_update_timestamp ON cg.strip;
DROP TRIGGER comic_geek_tg_update_timestamp ON cg.comic_geek;
DROP TRIGGER strip_geek_tg_update_timestamp ON cg.strip_geek;
DROP TRIGGER feed_tg_update_timestamp ON cg.feed;
DROP TRIGGER feed_comic_tg_update_timestamp ON cg.feed_comic;
DROP TRIGGER feed_strip_tg_update_timestamp ON cg.feed_strip;

DROP FUNCTION cg.update_timestamp();

COMMIT;
