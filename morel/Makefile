
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
