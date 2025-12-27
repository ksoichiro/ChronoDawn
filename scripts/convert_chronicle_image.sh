#!/bin/bash

# Chronicle Image Conversion Script
# Converts screenshots to Chronicle-style images for in-game guidebook display
#
# Usage:
#   ./scripts/convert_chronicle_image.sh <image_name>        # Convert single file
#   ./scripts/convert_chronicle_image.sh --all              # Convert all files
#
# Examples:
#   ./scripts/convert_chronicle_image.sh phantom_catacombs.png
#   ./scripts/convert_chronicle_image.sh --all

set -e

# Project root directory (one level up from script directory)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Input and output directories
INPUT_DIR="$PROJECT_ROOT/assets/screenshots/chronicle"
OUTPUT_DIR="$PROJECT_ROOT/common/src/main/resources/assets/chronodawn/textures/gui/chronicle"

# Check if ImageMagick is available
if ! command -v magick &> /dev/null; then
    echo "Error: ImageMagick is not installed"
    echo "On macOS: brew install imagemagick"
    exit 1
fi

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Function to convert an image
convert_image() {
    local filename="$1"
    local input_path="$INPUT_DIR/$filename"
    local output_path="$OUTPUT_DIR/$filename"

    if [ ! -f "$input_path" ]; then
        echo "Error: $input_path not found"
        return 1
    fi

    echo "Converting: $filename"

    magick "$input_path" \
        -colorspace Gray \
        -sketch 0x10+80 \
        -auto-level \
        -brightness-contrast -5x-10 \
        "$output_path"

    echo "Complete: $output_path"
}

# Main processing
if [ $# -eq 0 ]; then
    echo "Usage: $0 <image_name> or $0 --all"
    echo ""
    echo "Examples:"
    echo "  $0 phantom_catacombs.png"
    echo "  $0 --all"
    exit 1
fi

if [ "$1" == "--all" ]; then
    # Convert all files in directory
    echo "Converting all images in assets/screenshots/chronicle/..."
    echo ""

    if [ ! -d "$INPUT_DIR" ]; then
        echo "Error: $INPUT_DIR not found"
        exit 1
    fi

    count=0
    for file in "$INPUT_DIR"/*.png; do
        if [ -f "$file" ]; then
            filename=$(basename "$file")
            convert_image "$filename"
            count=$((count + 1))
            echo ""
        fi
    done

    echo "✓ Converted $count file(s)"
else
    # Convert specified file
    convert_image "$1"
fi
