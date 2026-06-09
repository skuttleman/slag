SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c
.ONESHELL:
.PHONY: help check-deps build-test lint test
.DEFAULT_GOAL := help

# Tools (can be overridden in the environment)
FOREMAN ?= foreman
NPM ?= npm
CLJ ?= clojure
NPX ?= npx
SASS ?= sass
LCOV ?= lcov
GENHTML ?= genhtml

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort \
	| awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

check-deps: ## Verify required CLI tools are available (with install hints)
	@echo "Checking required tools..."; \
	missing=""; \
	if ! command -v $(CLJ) >/dev/null 2>&1; then missing="$$missing $(CLJ)"; fi; \
	if [ -n "$$missing" ]; then \
		echo "Missing required tools:$$missing"; \
		echo ""; \
		echo "Install hints:"; \
		echo "  - Node & npm: https://nodejs.org/"; \
		echo "  - clojure: https://clojure.org/guides/getting_started"; \
		echo "  - foreman: gem install foreman OR 'npx foreman' (if you have npm)"; \
		echo "  - sass: npm i -g sass OR 'npx sass'"; \
		echo "  - lcov (genhtml): apt-get install lcov  OR brew install lcov"; \
		echo ""; \
		exit 1; \
	fi; \
	echo "All required tools available (or npx fallbacks present)."

build-test: ## Build test CLJS
	@echo "building cljs tests..."
	@$(CLJ) -A:shadow:test -M -m shadow.cljs.devtools.cli compile test

lint: check-deps ## Check codebase for linting errors
	@$(CLJ) -M:lint

test: check-deps lint build-test ## Run CLJS, server, and UI tests (requires clojure)
	@if [ "$(INCLUDE_CLJS_TESTS)" = "true" ]; then \
		echo "running CLJS tests..."; \
		$(CLJ) -M:test -m slag.test.cljs.runner; \
	fi
	@echo "running kaocha tests..."
	$(CLJ) -M:test -m kaocha.runner
