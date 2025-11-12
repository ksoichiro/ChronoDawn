#!/bin/bash

# Script to copy vanilla Minecraft textures from Gradle cache
# This extracts textures from the minecraft-merged JAR in Gradle cache

set -e

echo "=== Vanilla Texture Copier (Gradle Cache) ==="
echo ""

# Gradle cache directory for Fabric Loom
GRADLE_CACHE="$HOME/.gradle/caches/fabric-loom"

# Find minecraft-merged JAR for 1.21.1
MC_JAR=$(find "$GRADLE_CACHE" -name "minecraft-merged.jar" -path "*/1.21.1/*" | head -1)

if [ -z "$MC_JAR" ]; then
    echo "Error: minecraft-merged JAR not found in Gradle cache"
    echo "Please run: ./gradlew :fabric:build"
    echo "This will download the Minecraft assets to Gradle cache"
    exit 1
fi

echo "Found minecraft-merged JAR: $MC_JAR"
echo ""

# Output directory
OUTPUT_DIR="common/src/main/resources/assets/chronosphere/textures/entity/mobs"

# Create temporary directory
TEMP_DIR=$(mktemp -d)
echo "Using temporary directory: $TEMP_DIR"

# Extract textures from JAR
echo "Extracting textures..."
unzip -q "$MC_JAR" "assets/minecraft/textures/entity/zombie/zombie.png" -d "$TEMP_DIR" 2>/dev/null || true
unzip -q "$MC_JAR" "assets/minecraft/textures/entity/enderman/enderman.png" -d "$TEMP_DIR" 2>/dev/null || true
unzip -q "$MC_JAR" "assets/minecraft/textures/entity/wandering_trader.png" -d "$TEMP_DIR" 2>/dev/null || true

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Copy textures
echo ""
echo "Copying textures to mod directory..."

COPIED=0

if [ -f "$TEMP_DIR/assets/minecraft/textures/entity/zombie/zombie.png" ]; then
    cp "$TEMP_DIR/assets/minecraft/textures/entity/zombie/zombie.png" "$OUTPUT_DIR/temporal_wraith.png"
    echo "✓ temporal_wraith.png (64x64, from zombie.png)"
    COPIED=$((COPIED + 1))
else
    echo "✗ Failed to extract zombie.png"
fi

if [ -f "$TEMP_DIR/assets/minecraft/textures/entity/enderman/enderman.png" ]; then
    cp "$TEMP_DIR/assets/minecraft/textures/entity/enderman/enderman.png" "$OUTPUT_DIR/clockwork_sentinel.png"
    echo "✓ clockwork_sentinel.png (64x32, from enderman.png)"
    COPIED=$((COPIED + 1))
else
    echo "✗ Failed to extract enderman.png"
fi

if [ -f "$TEMP_DIR/assets/minecraft/textures/entity/wandering_trader.png" ]; then
    cp "$TEMP_DIR/assets/minecraft/textures/entity/wandering_trader.png" "$OUTPUT_DIR/time_keeper.png"
    echo "✓ time_keeper.png (64x64, from wandering_trader.png)"
    COPIED=$((COPIED + 1))
else
    echo "✗ Failed to extract wandering_trader.png"
fi

# Cleanup
rm -rf "$TEMP_DIR"

echo ""
echo "=== Done! ==="
echo "Copied $COPIED/3 textures to: $OUTPUT_DIR"
echo ""
echo "These are PLACEHOLDER textures. Please edit them with your own designs."
echo "After editing, rebuild with: ./gradlew :fabric:build"
