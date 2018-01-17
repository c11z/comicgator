
.PHONY: sbt
sbt: .localenv
	env $$(cat .localenv | xargs) sbt

.PHONY: docker
docker:
	sbt compile docker:publishLocal
