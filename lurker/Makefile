
.PHONY: sbt
sbt: .localenv
	env $$(cat .localenv | xargs) sbt

.PHONY: sbt-gcloud
sbt-gcloud: .gcloudenv
	env $$(cat .gcloudenv | xargs) sbt

.PHONY: docker-build
docker-build:
	sbt compile docker:publishLocal
