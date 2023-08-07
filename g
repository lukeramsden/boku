#!/usr/bin/env bash

set -euo pipefail

SCRIPT=$(realpath "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

export JAVA_HOME="$SCRIPTPATH/opt/java"
export PATH="$SCRIPTPATH/opt/.bin:$PATH"

"$SCRIPTPATH"/opt/bootstrap.sh

"$SCRIPTPATH"/gradlew "$@" || exit 1
