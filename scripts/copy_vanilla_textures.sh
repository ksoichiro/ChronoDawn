#!/bin/bash

# Script to copy vanilla Minecraft textures for custom mob placeholders
# This script extracts textures from Minecraft JAR file

set -e

echo "=== Vanilla Texture Copier for Chronosphere Mod ==="
echo ""

# Detect OS and set Minecraft directory
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    MC_DIR="$HOME/Library/Application Support/minecraft"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    MC_DIR="$HOME/.minecraft"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    # Windows (Git Bash)
    MC_DIR="$APPDATA/.minecraft"
else
    echo "Error: Unsupported OS"
    exit 1
fi

# Minecraft version
MC_VERSION="1.21.1"
MC_JAR="$MC_DIR/versions/$MC_VERSION/$MC_VERSION.jar"

# Output directory
OUTPUT_DIR="common/src/main/resources/assets/chronosphere/textures/entity/mobs"

# Check if Minecraft JAR exists
if [ ! -f "$MC_JAR" ]; then
    echo "Error: Minecraft JAR not found at: $MC_JAR"
    echo "Please ensure Minecraft 1.21.1 is installed."
    exit 1
fi

echo "Found Minecraft JAR: $MC_JAR"
echo ""

# Create temporary directory
TEMP_DIR=$(mktemp -d)
echo "Using temporary directory: $TEMP_DIR"

# Extract textures from JAR
echo "Extracting textures from Minecraft JAR..."
unzip -q "$MC_JAR" "assets/minecraft/textures/entity/zombie/zombie.png" -d "$TEMP_DIR" 2>/dev/null || true
unzip -q "$MC_JAR" "assets/minecraft/textures/entity/skeleton/skeleton.png" -d "$TEMP_DIR" 2>/dev/null || true
unzip -q "$MC_JAR" "assets/minecraft/textures/entity/wandering_trader.png" -d "$TEMP_DIR" 2>/dev/null || true

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Copy textures
echo ""
echo "Copying textures to mod directory..."

if [ -f "$TEMP_DIR/assets/minecraft/textures/entity/zombie/zombie.png" ]; then
    cp "$TEMP_DIR/assets/minecraft/textures/entity/zombie/zombie.png" "$OUTPUT_DIR/temporal_wraith.png"
    echo "✓ temporal_wraith.png (from zombie.png)"
else
    echo "✗ Failed to extract zombie.png"
fi

if [ -f "$TEMP_DIR/assets/minecraft/textures/entity/skeleton/skeleton.png" ]; then
    cp "$TEMP_DIR/assets/minecraft/textures/entity/skeleton/skeleton.png" "$OUTPUT_DIR/clockwork_sentinel.png"
    echo "✓ clockwork_sentinel.png (from skeleton.png)"
else
    echo "✗ Failed to extract skeleton.png"
fi

if [ -f "$TEMP_DIR/assets/minecraft/textures/entity/wandering_trader.png" ]; then
    cp "$TEMP_DIR/assets/minecraft/textures/entity/wandering_trader.png" "$OUTPUT_DIR/time_keeper.png"
    echo "✓ time_keeper.png (from wandering_trader.png)"
else
    echo "✗ Failed to extract wandering_trader.png"
fi

# Cleanup
rm -rf "$TEMP_DIR"

echo ""
echo "=== Done! ==="
echo "Textures copied to: $OUTPUT_DIR"
echo ""
echo "You can now edit these textures with an image editor."
echo "After editing, run: ./gradlew :fabric:runClient"
