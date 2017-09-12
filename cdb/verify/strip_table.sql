-- Verify strip_table

BEGIN;

SELECT id,
	comic_id,
	checksum,
	number,
	url,
	image_urls,
	thumbnail_image_url,
	title,
	image_alt,
	image_title,
	bonus_image_url,
	is_special,
	created_at,
	updated_at
 FROM cg.strip
 WHERE FALSE;

SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'strip_uq_comic_id_number';
SELECT 1/count(*) FROM pg_indexes WHERE indexname = 'strip_ix_updated_at';

SELECT has_table_privilege('mrcg', 'cg.strip', 'select');
SELECT has_table_privilege('mrcg', 'cg.strip', 'update');
SELECT has_table_privilege('mrcg', 'cg.strip', 'insert');

ROLLBACK;
