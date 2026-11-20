#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="${1:-$PWD}"

if [[ ! -f "${TARGET_DIR}/app/build.gradle.kts" ]]; then
  echo "ERROR: Expected NuvioTV root at '${TARGET_DIR}'" >&2
  exit 1
fi

mkdir -p \
  "${TARGET_DIR}/app/src/main/java/com/nuvio/tv/core/profile" \
  "${TARGET_DIR}/app/src/test/java/com/nuvio/tv/core/profile"

cp "${SCRIPT_DIR}/ExternalProfileSwitch.kt" \
   "${TARGET_DIR}/app/src/main/java/com/nuvio/tv/core/profile/ExternalProfileSwitch.kt"
cp "${SCRIPT_DIR}/ExternalProfileSwitchTest.kt" \
   "${TARGET_DIR}/app/src/test/java/com/nuvio/tv/core/profile/ExternalProfileSwitchTest.kt"

if git -C "${TARGET_DIR}" apply --check "${SCRIPT_DIR}/nuviotv-hooks.patch" 2>/dev/null; then
  git -C "${TARGET_DIR}" apply "${SCRIPT_DIR}/nuviotv-hooks.patch"
else
  echo "Clean patch check failed; attempting 3-way apply..." >&2
  git -C "${TARGET_DIR}" apply --3way "${SCRIPT_DIR}/nuviotv-hooks.patch"
fi

echo "FamilyHub patch applied cleanly to ${TARGET_DIR}."
