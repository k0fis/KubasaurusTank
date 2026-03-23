#!/bin/bash
# Deploy KubasaurusTank from GitHub Release
# Usage: ./deploy-kfstank.sh [target_dir]

set -e

REPO="k0fis/KubasaurusTank"
TARGET="${1:-$HOME/www/kuba/gm009}"
TAR_NAME="kfstank-web.tar.gz"

echo "=== Deploy KubasaurusTank ==="
echo "Target: $TARGET"

# Get latest release download URL
URL=$(curl -s "https://api.github.com/repos/$REPO/releases/latest" \
    | grep "browser_download_url.*$TAR_NAME" \
    | cut -d '"' -f 4)

if [ -z "$URL" ]; then
    echo "ERROR: No release found for $REPO"
    exit 1
fi

echo "Downloading: $URL"
curl -L -o /tmp/$TAR_NAME "$URL"

echo "Extracting to $TARGET"
mkdir -p "$TARGET"
rm -rf "$TARGET"/*
tar -xzf /tmp/$TAR_NAME -C "$TARGET"
rm -f /tmp/$TAR_NAME

echo "=== Deploy complete ==="
ls -la "$TARGET"
