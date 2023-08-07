#!/usr/bin/env bash

set -euo pipefail

SCRIPT=$(realpath "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

"$SCRIPTPATH"/g build -q
"$SCRIPTPATH"/opt/java/bin/java -jar "$SCRIPTPATH"/build/libs/boku-1.0-SNAPSHOT-fat.jar