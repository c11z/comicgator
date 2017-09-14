REPO = c11z/sqitch
TAG_LATEST = $(REPO)
TAG_POSTGRES = $(REPO):postgres
TAG_POSTGRES_SH = $(REPO):postgres-sh

.PHONY: build
build: build-latest build-postgres build-postgres-sh

.PHONY: build-latest
build-latest:
	docker build --tag $(TAG_LATEST) .

.PHONY: build-postgres
build-postgres:
	docker build --tag $(TAG_POSTGRES) postgres

.PHONY: build-postgres-sh
build-postgres-sh:
	docker build --tag $(TAG_POSTGRES_SH) postgres-sh

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
	postgres:9.6

.PHONY: sqitch
sqitch:
	docker run --rm \
	--interactive \
	--tty \
    --link cdb \
    --volume $$PWD:/src \
    c11z/sqitch:postgres-sh /bin/sh

.PHONY: psql
psql:
	PGOPTIONS=--search_path=cg psql --host localhost --port 5432 --username mrcg --dbname cdb

# .PHONY: push
# push: push-latest push-postgres push-postgres-sh

# .PHONY: push-latest
# push-latest:
# 	docker push $(TAG_LATEST)

# .PHONY: push-postgres
# push-postgres:
# 	docker push $(TAG_POSTGRES)

# .PHONY: push-postgres-sh
# push-postgres-sh:
# 	docker push $(TAG_POSTGRES_SH)

# TAG_CI_LATEST = $(CI_REGISTRY_IMAGE):$(CI_BUILD_REF_NAME)-latest
# TAG_CI_POSTGRES = $(CI_REGISTRY_IMAGE):$(CI_BUILD_REF_NAME)-postgres
# TAG_CI_POSTGRES_SH = $(CI_REGISTRY_IMAGE):$(CI_BUILD_REF_NAME)-postgres-sh

# .PHONY: login-ci
# login-ci:
# 	docker login --username gitlab-ci-token --password $(CI_BUILD_TOKEN) $(CI_REGISTRY)

# .PHONY: login-ci-hub
# login-ci-hub:
# 	docker login --username $(DOCKERHUB_USERNAME) --password $(DOCKERHUB_PASSWORD)

# .PHONY: pull-ci
# pull-ci: pull-ci-latest pull-ci-postgres pull-ci-postgres-sh

# .PHONY: pull-ci-latest
# pull-ci-latest:
# 	docker pull $(TAG_CI_LATEST)
# 	docker tag $(TAG_CI_LATEST) $(TAG_LATEST)

# .PHONY: pull-ci-postgres
# pull-ci-postgres:
# 	docker pull $(TAG_CI_POSTGRES)
# 	docker tag $(TAG_CI_POSTGRES) $(TAG_POSTGRES)

# .PHONY: pull-ci-postgres-sh
# pull-ci-postgres-sh:
# 	docker pull $(TAG_CI_POSTGRES_SH)
# 	docker tag $(TAG_CI_POSTGRES_SH) $(TAG_POSTGRES_SH)

# .PHONY: push-ci
# push-ci: push-ci-latest push-ci-postgres push-ci-postgres-sh

# .PHONY: push-ci-latest
# push-ci-latest:
# 	docker tag $(TAG_LATEST) $(TAG_CI_LATEST)
# 	docker push $(TAG_CI_LATEST)

# .PHONY: push-ci-postgres
# push-ci-postgres:
# 	docker tag $(TAG_POSTGRES) $(TAG_CI_POSTGRES)
# 	docker push $(TAG_CI_POSTGRES)

# .PHONY: push-ci-postgres-sh
# push-ci-postgres-sh:
# 	docker tag $(TAG_POSTGRES_SH) $(TAG_CI_POSTGRES_SH)
# 	docker push $(TAG_CI_POSTGRES_SH)
