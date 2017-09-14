
.PHONY: sbt
run: .localenv
	env $$(cat .localenv | xargs) sbt

.PHONY: docker-build
docker-build:
	sbt compile docker:publishLocal
