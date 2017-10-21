
include elm.mk

# .PHONY: clean
# clean:
# 	rm -rf $(BUILD_FOLDER)
# 	rm -rf $(DIST_FOLDER)
# 	rm -rf node_modules
# 	rm -f package-lock.json

.PHONY: docker-build
docker-build: $(DIST_TARGETS)
	docker build -t us.gcr.io/comic-gator/morel:latest .


$(BUILD_FOLDER)/boot.js: src/boot.js
	cp $? $@


$(DIST_FOLDER)/boot.min.js: src/boot.js $(NODE_BIN_DIRECTORY)/uglifyjs
	$(NODE_BIN_DIRECTORY)/uglifyjs --compress --mangle --output $@ -- src/boot.js
