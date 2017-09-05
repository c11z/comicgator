SHELL = /bin/sh
DEFAULT_BROWSER = firefox
DIST = ./dist
SRC = ./src

MAIN_ELM = $(SRC)/Main.elm
ELM_JS = $(SRC)/elm.js
INDEX = $(SRC)/index.html
ASSETS := $(wildcard $(SRC)/assets/*)
CSS = $(SRC)/custom.css

.SUFFIXES: .elm .js .html .css .svg

.PHONY: all
all: clean build run 

.PHONY: build
build: $(ELM_JS) dist

$(ELM_JS): $(MAIN_ELM)
	@rm -f $(ELM_JS)
	@elm-make $(MAIN_ELM) --yes --output=$(ELM_JS)

dist: $(ASSETS) $(CSS) $(ELM_JS) $(INDEX) 
	@mkdir -p $(DIST)
	@echo "Creating assets versioning file -> $@"
	@cp $(INDEX) $(DIST)/index.html
	@for path in $$(find $(SRC) -type f ! -name "*.elm" ! -name "*.html"); do \
		file=$$(basename $$path); \
		new=$${file%.*}-$$(shasum $$path | cut -c1-8).$${file##*.}; \
		cp $$path $(DIST)/$$new; \
		echo "s/$${file}/$${new}/g"; \
		sed -i -e "s/$${file}/$${new}/g" $(DIST)/index.html; \
        	echo "$$path $$new" >> $(DIST)/manifest; \
    	done

.PHONY: run
run: dist
	$(DEFAULT_BROWSER) -new-tab $(DIST)/index.html

.PHONY: clean
clean:
	@echo "Cleaning..."
	@rm -rf $(ELM_JS)
	@rm -rf $(DIST)
	@rm -rf elm-stuff
