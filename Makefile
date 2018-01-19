.PHONY: deploy
deploy: publishlocal push pullup

publishlocal: lurker
	$(MAKE) docker -C lurker

.PHONY: push
push:
	docker push comicgator/lurker

.PHONY: pullup
pullup:
	ssh root@comicgator.com 'docker-compose pull && docker-compose up -d'

.PHONY: logs
logs:
	ssh -t root@comicgator.com 'COMPOSE_HTTP_TIMEOUT=600 docker-compose logs -f'

.PHONY: cdb
cdb:
	ssh -t root@comicgator.com './cdb'
