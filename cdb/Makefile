TARGET_POSTGRES = postgres:9.6

.PHONY: run
run: cdb sleep-10 sqitch-deploy

.PHONY: cdb
cdb:
	docker run --rm \
	--detach \
	--name cdb \
	--publish 5432:5432 \
	--env POSTGRES_USER=mrcg \
    --env POSTGRES_PASSWORD=mrcg \
    --env POSTGRES_DB=cdb \
    --env PGDATA=/var/lib/postgresql/data \
	$(TARGET_POSTGRES)

.PHONY: sleep-10
sleep-10:
	sleep 10

.PHONY: psql
psql:
	PGOPTIONS=--search_path=cg \
	psql \
	--host localhost \
	--port 5432 \
	--username mrcg \
	--dbname cdb
