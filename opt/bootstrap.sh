#!/usr/bin/env bash

set -euo pipefail

BOOTSTRAP_START_TIME=$(date +%s)

OPT_DIR="$(dirname "$(readlink -f "$0")")"

cd "$OPT_DIR"

rm -r ".download" &>/dev/null || true
mkdir -p ".download"

ARCH="$(arch)"
UNAME="$(uname)"

#START JAVA
JAVA_VERSION="17.0.6.10.1"
if [ "$UNAME" = "Linux" ] && [ "$ARCH" = "x86_64" ]; then
  JAVA_SIGNATURE="365bb4ae3f56bfb3c0df5f8f5b809ff0212366c46970c4b371acb80ecf4706cc"
  JAVA_BINARY="amazon-corretto-$JAVA_VERSION-linux-x64.tar.gz"
  JAVA_STRIP_COMPONENTS=1
elif [ "$UNAME" = "Linux" ] && [ "$ARCH" = "aarch64" ]; then
  JAVA_SIGNATURE="8fc36009858cfb4dbd30ba4847c6fc4d53d4f843b08dea8189f38fbf8bf40ca8"
  JAVA_BINARY="amazon-corretto-$JAVA_VERSION-linux-aarch64.tar.gz"
  JAVA_STRIP_COMPONENTS=1
elif [ "$UNAME" = "Darwin" ] && [ "$ARCH" = "arm64" ]; then
  JAVA_SIGNATURE="f7411c1d8a94681e669b133ab57a7ef815aa145b3ecc041c93ca7ff1eb1811b3"
  JAVA_BINARY="amazon-corretto-$JAVA_VERSION-macosx-aarch64.tar.gz"
  JAVA_STRIP_COMPONENTS=3
elif [ "$UNAME" = "Darwin" ] && [ "$ARCH" = "x86_64" ]; then
  JAVA_SIGNATURE="1ba7e50d74c2f402431d365eb8e5f7b860b03b18956af59f5f364f6567a8463e"
  JAVA_BINARY="amazon-corretto-$JAVA_VERSION-macosx-x64.tar.gz"
  JAVA_STRIP_COMPONENTS=3
else
  echo "Couldn't resolve Java binary for $UNAME $ARCH"
  exit 1
fi

JAVA_URL="https://corretto.aws/downloads/resources/$JAVA_VERSION/$JAVA_BINARY"

ACTUAL_JAVA_SHA=""
if [[ -e "java.sha256" ]]; then
  ACTUAL_JAVA_SHA=$(cat "java.sha256")
fi

if [[ "$ACTUAL_JAVA_SHA" != "$JAVA_SIGNATURE" ]]; then
  echo "Downloading Java..."
  curl --fail --location --progress-bar --output ".download/$JAVA_BINARY" "$JAVA_URL"
  HASH_OF_DOWNLOADED_ZIP="$(sha256sum ".download/$JAVA_BINARY" | awk '{ print $1 }')"
  if [ "$HASH_OF_DOWNLOADED_ZIP" != "$JAVA_SIGNATURE" ]; then
    echo "Expected hash for Java to be $JAVA_SIGNATURE, was $HASH_OF_DOWNLOADED_ZIP"
    exit 1
  fi

  echo "Extracting Java..."
  rm -rf "java-$JAVA_VERSION" &>/dev/null || true
  mkdir -p "java-$JAVA_VERSION"
  tar -xf ".download/$JAVA_BINARY" -C "java-$JAVA_VERSION" --strip-components=$JAVA_STRIP_COMPONENTS --no-same-owner
  ln -sf "java-$JAVA_VERSION" "java"
  echo $JAVA_SIGNATURE >"java.sha256"
fi
#END JAVA

rm -r ".download" # delete temp download directory

mkdir -p "$OPT_DIR"/.bin

# Remove existing symlinks in bin
mkdir -p .bin
find ./.bin/ -maxdepth 1 -type l -delete

# Symlink all binaries in to .bin
ln -s "$OPT_DIR"/java/bin/* ".bin/"

BOOTSTRAP_END_TIME=$(date +%s)
BOOTSTRAP_DURATION=$((BOOTSTRAP_END_TIME - BOOTSTRAP_START_TIME))

if [[ ${BOOTSTRAP_DURATION} -gt 1 ]]; then
  echo "Bootstrap time: $BOOTSTRAP_DURATION seconds"
fi
