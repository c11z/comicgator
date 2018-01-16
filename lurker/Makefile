
.PHONY: sbt
sbt: .localenv
	env $$(cat .localenv | xargs) sbt

.PHONY: sbt-production
sbt-gcloud: .productionenv
	env $$(cat .productionenv | xargs) sbt

.PHONY: docker-build
docker-build:
	sbt compile docker:publishLocal
