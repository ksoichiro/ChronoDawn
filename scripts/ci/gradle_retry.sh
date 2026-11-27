#!/usr/bin/env bash
# Runs ./gradlew with the given arguments and runs it again, up to four times,
# when a dependency download failed. The nightly workflow starts about 36 jobs
# at once from shared GitHub runner addresses, and Maven Central and the
# Minecraft library server rate limit them. Gradle reports that either as
# "status code 429" or, for some repositories, as "Could not find <pom>".
# Any other failure is returned at once, so a real compile or test failure is
# not run four times. A rate limit can last minutes, so the wait grows by two
# minutes per attempt.
set -uo pipefail

pattern='status code (429|5[0-9][0-9])|Could not (resolve|find|GET|HEAD) |Read timed out|Connection reset'
log=$(mktemp "${TMPDIR:-/tmp}/gradle-retry.XXXXXX")
for attempt in 1 2 3 4; do
  if ./gradlew "$@" 2>&1 | tee "$log"; then
    exit 0
  fi
  if ! grep -Eq "$pattern" "$log"; then
    exit 1
  fi
  echo "::warning::Dependency download failed (rate limit or network), attempt $attempt of 4."
  [ "$attempt" -lt 4 ] && sleep $((attempt * 120))
done
exit 1
