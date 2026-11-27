#!/usr/bin/env bash
# Runs ./gradlew with the given arguments and runs it again when a dependency
# download failed with HTTP 429 (Too Many Requests). The nightly workflow starts
# about 36 jobs at once from shared GitHub runner addresses, and Maven Central
# rate limits them. Any other failure is returned at once, so a real build or
# test failure is not run three times.
set -uo pipefail

log=$(mktemp "${TMPDIR:-/tmp}/gradle-retry.XXXXXX")
for attempt in 1 2 3; do
  if ./gradlew "$@" 2>&1 | tee "$log"; then
    exit 0
  fi
  if ! grep -q 'status code 429' "$log"; then
    exit 1
  fi
  echo "::warning::Dependency download was rate limited (HTTP 429), attempt $attempt of 3."
  sleep $((attempt * 60))
done
exit 1
