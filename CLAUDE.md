# Chronosphere Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-10-19

## Active Technologies
- Java 21 (Minecraft Java Edition 1.21.1) + NeoForge 21.1.x, Custom Portal API Reforged, mcjunitlib (001-chronosphere-mod)

## Project Structure
```
src/
tests/
```

## Commands
# Add commands for Java 21 (Minecraft Java Edition 1.21.1)

## Code Style
Java 21 (Minecraft Java Edition 1.21.1): Follow standard conventions

## Recent Changes
- 001-chronosphere-mod: Added Java 21 (Minecraft Java Edition 1.21.1) + NeoForge 21.1.x, Custom Portal API Reforged, mcjunitlib
- 2025-10-23: Migrated to Groovy DSL and Mojang mappings for Minecraft 1.21.1 compatibility

## Build Configuration
- **Build DSL**: Groovy DSL (not Kotlin DSL) - for compatibility with Architectury Loom 1.11-SNAPSHOT
- **Mappings**: Mojang mappings (not Yarn) - code uses official Minecraft class names (e.g., `net.minecraft.core.Registry`)
- **Shadow Plugin**: com.gradleup.shadow 8.3.6 - for bundling common module into platform-specific JARs

## Development Notes
- When writing code, use Mojang mapping names (e.g., `net.minecraft.world.level.Level`, not Yarn's `class_XXXX`)
- Build files use Groovy syntax (e.g., `maven { url 'https://...' }`, not `maven { url = "https://..." }`)
- Common module code is bundled into Fabric JAR using Shadow plugin

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
