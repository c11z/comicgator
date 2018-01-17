-- Revert update_timestamp_trigger

BEGIN;

DROP TRIGGER comic_tg_update_timestamp ON cg.comic;
DROP TRIGGER geek_tg_update_timestamp ON cg.geek;
DROP TRIGGER strip_tg_update_timestamp ON cg.strip;
DROP TRIGGER feed_tg_update_timestamp ON cg.feed;
DROP TRIGGER feed_comic_tg_update_timestamp ON cg.feed_comic;
DROP TRIGGER feed_strip_tg_update_timestamp ON cg.feed_strip;

DROP FUNCTION cg.update_timestamp();

COMMIT;
