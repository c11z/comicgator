SELECT *
FROM pg_stat_activity;

SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'cdb'
      AND pid <> pg_backend_pid();


SELECT sum(numbackends)
FROM pg_stat_database;

-- insert example for strip
INSERT INTO cg.strip (
  comic_id,
  checksum,
  number,
  url,
  image_urls,
  thumbnail_image_url,
  title, image_alt,
  image_title,
  bonus_image_url,
  is_special)
VALUES (
  '57341408de7af93a83733280',
  '58ae628cd3b2ee02f3ed44a5a1911c93',
  5,
  'http://xkcd.com/1719/',
  '{"http://imgs.xkcd.com/comics/superzoom.png"}',
  '',
  'Superzoom3',
  'Superzoom',
  '*click* Let him know he''s got a stain on his shirt, though.',
  NULL,
  'false')
ON CONFLICT (comic_id, number)
  DO
  UPDATE SET (
    comic_id,
    checksum,
    number,
    url,
    image_urls,
    thumbnail_image_url,
    title, image_alt,
    image_title,
    bonus_image_url,
    is_special) = (
    '57341408de7af93a83733280',
    '58ae628cd3b2ee02f3ed44a5a1911c93',
    5,
    'http://xkcd.com/1719/',
    '{"http://imgs.xkcd.com/comics/superzoom.png"}',
    '',
    'Superzoom3',
    'Superzoom',
    '*click* Let him know he''s got a stain on his shirt, though.',
    NULL,
    'false');


INSERT INTO cg.comic (id, hostname, title, creator, is_advertised, patreon_url, store_url, banner_image, first_url)
VALUES ('57341408de7af93a83733280',
        'xkcd.com',
        'XKCD',
        'Randall Monroe',
        'false',
        NULL,
        'http://store.xkcd.com/',
        '',
        'http://xkcd.com/1/')

ON CONFLICT (id)
  DO
  UPDATE
    SET (id, hostname, title, creator, is_advertised, patreon_url, store_url, banner_image, first_url)
    = ('57341408de7af93a83733280',
       'xkcd.com',
       'XKCD',
       'Randall Monroe',
       'false',
       NULL,
       'http://store.xkcd.com/',
       '',
       'http://xkcd.com/1/');

SELECT array_to_json(array_agg(row_to_json(comic_select))) AS comics
FROM (
       SELECT
         c.id,
         c.hostname,
         c.title,
         c.creator,
         c.is_advertised,
         c.patreon_url,
         c.store_url,
         c.banner_image,
         COALESCE(cg.is_viewed, FALSE)  AS is_viewed,
         COALESCE(cg.is_starred, FALSE) AS is_starred,
         c.created_at,
         c.updated_at
       FROM cg.comic c
         LEFT JOIN cg.comic_geek cg ON c.id = cg.comic_id
       WHERE cg.geek_id = '57c3b1a0976f6d0a5463785b' OR cg.geek_id IS NULL
       ORDER BY c.id ASC
     ) comic_select;

INSERT INTO cg.geek (email, is_receptive)
VALUES ('corydominguez@gmail.com', 'true' :: BOOLEAN)
ON CONFLICT (email)
  DO
  UPDATE
    SET (email, is_receptive) = ('corydominguez@gmail.com', 'true' :: BOOLEAN);


SELECT g.id
FROM cg.verification v
  JOIN cg.geek g USING (email)
WHERE v.email = 'corydominguez@gmail.com'
      AND v.code = '123456';


INSERT INTO cg.session_geek (geek_id, token)
VALUES ('582798ceb5b84a016b51951d', 'a798ca6cc1bf2c3cbde87e454502b014');

WITH
    validate AS (
      SELECT g.id
      FROM cg.verification v
        JOIN cg.geek g USING (email)
      WHERE v.email = 'corydominguez@gmail.com'
            AND v.code = '123456'),
    cleanup AS (
    DELETE FROM cg.verification
    WHERE email = 'corydominguez@gmail.com')
SELECT *
FROM validate;

INSERT INTO cg.verification (email, code) VALUES ('corydominguez@gmail.com', '123456')
ON CONFLICT (email)
  DO
  UPDATE SET (email, code) = ('corydominguez@gmail.com', '123456');


SELECT array_to_json(array_agg(row_to_json(strip_select))) AS strips
FROM (
       SELECT
         s.id,
         s.comic_id,
         s.checksum,
         s.number,
         s.url,
         s.image_urls,
         s.thumbnail_image_url,
         s.title,
         s.image_alt,
         s.image_title,
         s.bonus_image_url,
         s.is_special,
         COALESCE(sg.is_viewed, FALSE)  AS is_viewed,
         COALESCE(sg.is_starred, FALSE) AS is_starred,
         s.created_at,
         s.updated_at
       FROM cg.strip s LEFT JOIN cg.strip_geek sg ON s.id = sg.strip_id
       WHERE s.comic_id = '57341408de7af93a83733280' AND (sg.geek_id = '57c3b1a0976f6d0a5463785b' OR sg.geek_id IS NULL)
       ORDER BY s.number ASC
     ) strip_select;

TRUNCATE cg.strip, cg.strip_geek;

SELECT *
FROM cg.generate_object_id();


SELECT array_to_json(array_agg(row_to_json(feed_select))) AS feeds
FROM (
       SELECT
         f.id,
         f.name,
         f.created_at,
         f.updated_at
       FROM cg.feed f
       WHERE f.geek_id = '586613d161fa970070034bf4'
       ORDER BY f.id ASC
     ) feed_select;

INSERT INTO cg.feed (
  geek_id,
  name)
VALUES (
  '586613d161fa970070034bf4',
  'soft chain')
RETURNING json_build_object(
    'id', id,
    'name', name,
    'created_at', created_at,
    'updated_at', updated_at);

UPDATE cg.feed f
SET name = 'soft carriage'
WHERE f.id = '587889dad4a9b400edba7589'
      AND f.geek_id = '586613d161fa970070034bf4'
RETURNING json_build_object(
    'id', id,
    'name', name,
    'created_at', created_at,
    'updated_at', updated_at);

-- saveFeedComic
DO $$
DECLARE
  _is_latest BOOLEAN := TRUE;
  _cron      TEXT := '* * * * *';
BEGIN

  INSERT INTO cg.feed_comic (feed_id, comic_id, is_latest, cron, mark, step)
  VALUES ('5878a7fe90589501ac65f5ab', '57341408de7af93a83733280', _is_latest, _cron, 1, 5)
  ON CONFLICT (feed_id, comic_id)
    DO UPDATE SET (is_latest, cron, mark, step) = (_is_latest, _cron, 1, 5);

  IF _is_latest
  THEN
    INSERT INTO cg.feed_strip (feed_id, strip_id)
    VALUES (
      '5878a7fe90589501ac65f5ab',
      (
        SELECT s.id
        FROM cg.strip s
        WHERE s.comic_id = '57341408de7af93a83733280'
        ORDER BY s.number DESC
        LIMIT 1
      )
    )
    ON CONFLICT (feed_id, strip_id)
      DO UPDATE SET (feed_id) = ('5878a7fe90589501ac65f5ab');
  END IF;

  IF _cron IS NOT NULL
  THEN
    INSERT INTO cg.feed_strip (feed_id, strip_id)
    VALUES (
      '5878a7fe90589501ac65f5ab',
      (
        SELECT s.id
        FROM cg.strip s
        WHERE s.comic_id = '57341408de7af93a83733280'
              AND s.number = 1
      )
    );
  END IF;
END $$;

SELECT fc.feed_id, fc.comic_id FROM cg.feed_comic fc WHERE fc.is_latest = TRUE;

-- selectCronFeedComics

SELECT fc.feed_id, fc.comic_id, fc.cron, fc.mark, fc.step FROM cg.feed_comic fc WHERE fc.cron IS NOT NULL;


-- latestFeedStrip
DO $$
DECLARE
  rec cg.feed_comic%ROWTYPE;
BEGIN
  FOR rec IN EXECUTE 'SELECT * FROM cg.feed_comic fc WHERE fc.is_latest = TRUE'
  LOOP
    INSERT INTO cg.feed_strip (feed_id, strip_id)
      (
        SELECT
          rec.feed_id,
          s.id
        FROM cg.strip s
        WHERE s.comic_id = rec.comic_id
        AND s.number > (
          SELECT s.number
          FROM cg.feed_strip fs
            JOIN cg.strip s ON fs.strip_id = s.id
          WHERE s.comic_id = rec.comic_id
          ORDER BY s.number DESC
          LIMIT 1
        )
      );
  END LOOP;
END $$;

-- catchUpLatestFeedStrip
BEGIN;

INSERT INTO cg.feed_strip (feed_id, strip_id) (
  SELECT
    '5878a7fe90589501ac65f5ab',
    s.id
  FROM cg.strip s
  WHERE s.comic_id = '57341408de7af93a83733280'
        AND s.number > 1 AND s.number <= 1 + 5)
ON CONFLICT (feed_id, strip_id)
  DO UPDATE SET (feed_id) = ('5878a7fe90589501ac65f5ab');

UPDATE cg.feed_comic
SET mark = 1 + 5
WHERE feed_id = '5878a7fe90589501ac65f5ab'
AND comic_id = '57341408de7af93a83733280';

COMMIT;

ROLLBACK;

SELECT s.number
FROM cg.feed_strip fs
JOIN cg.strip s ON fs.strip_id = s.id
ORDER BY s.number DESC
LIMIT 1;

-- Get max value row for a given column and filters. Turns out the order and limit 1 is much faster for small datasets.
-- since we filter on the indexed comic_id we can expect that the ordered dataset will only ever be in the thousands.
EXPLAIN ANALYSE SELECT s.id
                FROM cg.strip s
                WHERE s.comic_id = '57341408de7af93a83733280'
                ORDER BY s.number DESC
                LIMIT 1;

EXPLAIN ANALYSE SELECT a.*
                FROM cg.strip a LEFT JOIN cg.strip b ON a.number < b.number
                WHERE b.id IS NULL AND a.comic_id = '57341408de7af93a83733280';