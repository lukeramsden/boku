#!/usr/bin/env bash

set -euo pipefail

SCRIPT=$(realpath "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

git pull -r
"$SCRIPTPATH"/g build -q
git push