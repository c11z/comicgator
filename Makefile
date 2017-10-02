SUBDIRS = morel 

.PHONY: subdirs $(SUBDIRS)
subdirs: $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@

.PHONY: integration
integration:
	curl --head --request GET 'http://localhost:9000/comics';
	curl --request GET 'http://localhost:9000/comics' | jq;
	curl --include \
	--header 'Content-Type: application/json' \
	--request POST 'http://localhost:9000/feeds' \
	--data '{ "email": "corydominguez@gmail.com", "comic_id": "57341408de7af93a83733280", "is_latest": true, "is_replay": true, "mark": 1, "step": 10}'
