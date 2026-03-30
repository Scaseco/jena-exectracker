CWD = $(shell pwd)

POM = -f pom.xml
# Maven Clean Install Skip ; skip tests, javadoc, scaladoc, etc
MS = mvn -DskipTests -Dmaven.javadoc.skip=true -Dskip
MCIS = $(MS) clean install
MCCS = $(MS) clean compile

VER = $(error specify VER=releasefile-name e.g. VER=1.9.7-rc2)
loud = echo "@@" $(1);$(1)

# Source: https://stackoverflow.com/questions/4219255/how-do-you-get-the-list-of-targets-in-a-makefile
.PHONY: help

.ONESHELL:
help:   ## Show these help instructions
	@sed -rn 's/^([a-zA-Z_-]+):.*?## (.*)$$/"\1" "\2"/p' < $(MAKEFILE_LIST) | xargs printf "make %-20s# %s\n"

fuseki-plugin: ## Create only the standalone jar-with-dependencies for the Fuseki Mod
	$(MCCS) $(POM) package -Pbundle -pl :jena-exectracker-pkg-fuseki-plugin -am $(ARGS)
	file=`find '$(CWD)/jena-exectracker-pkg-fuseki-plugin/target' -name '*-fuseki-plugin*.jar'`
	printf '\nCreated package:\n\n%s\n\n' "$$file"

release-github: SHELL:=/bin/bash
release-github: ## Create files for Github upload
	@set -eu
	ver=$(VER)
	$(call loud,$(MAKE) uberjar-fuseki-mod)
	file=`find '$(CWD)/jena-exectracker-pkg-fuseki-plugin/target' -name '*-fuseki-plugin*.jar'`
	$(call loud,cp "$$file" "jena-exectracker-fuseki-mod-$$ver.jar")
	$(call loud,gh release create v$$ver "jena-exectracker-fuseki-mod-$$ver.jar")

