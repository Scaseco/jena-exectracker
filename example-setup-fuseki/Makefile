BASE = apache-jena-fuseki-6.0.0

# ----------------------------------------------------------------------
# Variables
# ----------------------------------------------------------------------
CWD            := $(shell pwd)
PARENT_DIR     := $(CWD)/..
FUSEKI_ARCHIVE := $(BASE).tar.gz
FUSEKI_BASE    := target/fuseki
DOWNLOADS      := target/downloads

# The jar produced by the parent build
PLUGIN_JAR_PATTERN := $(PLUGIN_SRC_DIR)/target/*-jar-with-dependencies.jar

# Where the jar should be installed inside Fuseki
FUSEKI_JAR_INSTALLED_PATTERN := $(FUSEKI_BASE)/run/extra/%-jar-with-dependencies.jar

# The jar that the parent build creates (wildcard works even if the version changes)
PLUGIN_SRC_DIR := $(PARENT_DIR)/jena-exectracker-pkg-fuseki-plugin

# Source: https://stackoverflow.com/questions/4219255/how-do-you-get-the-list-of-targets-in-a-makefile
SHELL := /bin/bash
.PHONY: help build-fuseki-plugin clean-fuseki-plugin setup-fuseki copy-example-config copy-fuseki-plugin
.SILENT: copy-example-config setup-example

.ONESHELL:
help:   ## Show these help instructions
	@sed -rn 's/^([a-zA-Z_-]+):.*?## (.*)$$/"\1" "\2"/p' < $(MAKEFILE_LIST) | xargs printf "make %-20s# %s\n"

target:
	mkdir -p target

$(DOWNLOADS):
	mkdir -p "$(DOWNLOADS)"

$(DOWNLOADS)/$(FUSEKI_ARCHIVE).complete: | $(DOWNLOADS)
	curl -C - -L -O "https://dlcdn.apache.org/jena/binaries/$(FUSEKI_ARCHIVE)" --output-dir "$(DOWNLOADS)"
	touch "$(DOWNLOADS)/$(FUSEKI_ARCHIVE).complete"

$(FUSEKI_BASE)/setup.complete: $(DOWNLOADS)/$(FUSEKI_ARCHIVE).complete ## Setup Fuseki with the GraphQl4Sparql Fuseki Mod and example data
	mkdir -p "$(FUSEKI_BASE)"
	tar xf "$(DOWNLOADS)/$(FUSEKI_ARCHIVE)" -C $(FUSEKI_BASE) --strip-components=1
	touch "$(FUSEKI_BASE)/setup.complete"

setup-fuseki: $(FUSEKI_BASE)/setup.complete

copy-example-config: setup-fuseki
	mkdir -p "$(FUSEKI_BASE)/run/configuration"
	# Note: "--update=none" means "Do not overwrite existing files"
	cp --update=none config.ttl $(FUSEKI_BASE)/run
	cp --update=none passwd.txt $(FUSEKI_BASE)/run
	cp --update=none exectracker-demo_ds.ttl "$(FUSEKI_BASE)/run/configuration"
	mkdir -p "$(FUSEKI_BASE)/run/content"
	cp --update=none mona-lisa.ttl "$(FUSEKI_BASE)/run/content"

copy-fuseki-plugin: $(FUSEKI_BASE)/run/extra/jena-exectracker-fuseki-plugin.jar

setup-example: copy-example-config copy-fuseki-plugin ## Setup the example
	PORT=3030
	echo "------------------------"
	echo "Fuseki set up! Start it with:"
	echo
	echo "(cd $(FUSEKI_BASE) && ./fuseki-server --config=run/config.ttl --port $${PORT})"
	echo
	echo "The ExecTracker UI will be available at http://localhost:$${PORT}/example-exectracker/tracker"
	echo
	echo "Querying the endpoint will show ongoing executions in the ExecTracker UI."
	echo
	echo "curl http://localhost:$${PORT}/example-exectracker -G --data-urlencode 'query=SELECT * { ?s ?p ?o } LIMIT 2'"
	echo
	echo "And so will updates:"
	echo
	echo "curl -X POST http://localhost:$${PORT}/example-exectracker/update -u 'test:test' --data-urlencode 'update=INSERT DATA { <x> <y> <z> }'"

$(FUSEKI_BASE)/run/extra:
	mkdir -p "$(FUSEKI_BASE)/run/extra"

$(FUSEKI_BASE)/run/extra/jena-exectracker-fuseki-plugin.jar: $(FUSEKI_BASE)/setup.complete | $(FUSEKI_BASE)/run/extra
	@$(MAKE) -C $(PARENT_DIR) fuseki-plugin
	file=`find '$(PARENT_DIR)/jena-exectracker-pkg-fuseki-plugin/target' -name '*-fuseki-plugin*.jar'`
	cp "$$file" "$(FUSEKI_BASE)/run/extra/jena-exectracker-fuseki-plugin.jar"

build-fuseki-plugin: $(FUSEKI_BASE)/run/extra/jena-exectracker-fuseki-plugin.jar

clean-fuseki-plugin:
	rm -f $(FUSEKI_BASE)/run/extra/jena-exectracker-fuseki-plugin.jar

