#!/bin/bash
#
# Generate an animated sprite sheet from a single texture.
# Usage: ./scripts/animation.sh <texture_name>
# Example: ./scripts/animation.sh clockstone_block
#
# The source PNG must exist at assets/<texture_name>.png.
# Outputs:
#   common/shared/src/main/resources/assets/chronodawn/textures/block/<texture_name>.png
#   common/shared/src/main/resources/assets/chronodawn/textures/block/<texture_name>.png.mcmeta

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

TEXTURE_NAME="${1:?Usage: $0 <texture_name>}"
SOURCE_PNG="$PROJECT_ROOT/assets/${TEXTURE_NAME}.png"
OUTPUT_DIR="$PROJECT_ROOT/common/shared/src/main/resources/assets/chronodawn/textures/block"
OUTPUT_PNG="$OUTPUT_DIR/${TEXTURE_NAME}.png"
OUTPUT_MCMETA="$OUTPUT_DIR/${TEXTURE_NAME}.png.mcmeta"

if [ ! -f "$SOURCE_PNG" ]; then
  echo "Error: Source file not found: $SOURCE_PNG" >&2
  echo "Copy the original texture to assets/${TEXTURE_NAME}.png first." >&2
  exit 1
fi

TMPDIR="$(mktemp -d)"
trap 'rm -rf "$TMPDIR"' EXIT

# Generate frames with sine-eased brightness/saturation pulsing.
# -modulate brightness,saturation,hue (100 = unchanged)
#
# Parameters:
FRAME_COUNT=20   # Total frames in one cycle
AMPLITUDE=15     # Max change from base (100 + 15 = 115 at peak)

# Compute modulate values using sine easing: sin(π * i / N) gives 0→1→0
for i in $(seq 0 $((FRAME_COUNT - 1))); do
  modulate=$(awk -v i="$i" -v n="$FRAME_COUNT" -v amp="$AMPLITUDE" \
    'BEGIN { v = 100 + amp * sin(3.14159265 * i / n); printf "%d,%d,100", v, v }')
  magick "$SOURCE_PNG" -modulate "$modulate" "$TMPDIR/frame_$(printf '%02d' "$i").png"
done

# Vertically append all frames into a sprite sheet
magick "$TMPDIR"/frame_*.png -append "$OUTPUT_PNG"

# Generate mcmeta
cat > "$OUTPUT_MCMETA" <<'EOF'
{
  "animation": {
    "frametime": 2
  }
}
EOF

echo "Generated: $OUTPUT_PNG"
echo "Generated: $OUTPUT_MCMETA"
