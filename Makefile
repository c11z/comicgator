.PHONY: deploy
deploy: docker push pullup

docker: lurker
	$(MAKE) docker -C lurker

.PHONY: push
push:
	docker push comicgator/lurker

.PHONY: pullup
pullup:
	ssh root@comicgator.com 'docker-compose pull && docker-compose up -d'

.PHONY: logs
logs:
	ssh -t root@comicgator.com 'docker-compose logs -f'

.PHONY: cdb
cdb:
	ssh -t root@comicgator.com './cdb'
