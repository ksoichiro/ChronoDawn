# Contributing to Chrono Dawn

Thank you for your interest in contributing to Chrono Dawn! This guide will help you get started.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Workflow](#development-workflow)
4. [Coding Standards](#coding-standards)
5. [Submitting Changes](#submitting-changes)
6. [Translations](#translations)
7. [License](#license)

---

## Code of Conduct

By participating in this project, you agree to be respectful and constructive in all interactions. We aim to create a welcoming environment for contributors of all backgrounds and skill levels.

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK) 21**
- **Git**
- **IntelliJ IDEA** (recommended) or Eclipse

### Clone and Setup

```bash
# Clone the repository
git clone https://github.com/ksoichiro/ChronoDawn.git
cd ChronoDawn

# Build the project
./gradlew build

# Run the Fabric client (for testing)
./gradlew :fabric:runClient

# Run the NeoForge client (for testing)
./gradlew :neoforge:runClient
```

### Project Structure

Chrono Dawn uses the **Architectury** framework for multi-loader support:

```
common/     - Shared code (~80% of implementation)
fabric/     - Fabric-specific code (~10%)
neoforge/   - NeoForge-specific code (~10%)
```

For detailed technical documentation, see [Developer Guide](docs/developer_guide.md).

---

## Development Workflow

### 1. Find or Create an Issue

- Check [existing issues](https://github.com/ksoichiro/ChronoDawn/issues) for tasks
- If you have a new idea, open an issue first to discuss it
- Look for issues labeled `good first issue` if you're new to the project

### 2. Create a Branch

```bash
# Create a feature branch
git checkout -b feature/your-feature-name

# Or a bugfix branch
git checkout -b fix/your-bugfix-name
```

### 3. Make Your Changes

- Follow the [coding standards](#coding-standards) below
- Write clear, self-documenting code
- Add comments only where logic is non-obvious
- Test your changes on both Fabric and NeoForge (if applicable)

### 4. Test Your Changes

```bash
# Run tests
./gradlew test

# Build and verify
./gradlew build

# Test in-game (Fabric)
./gradlew :fabric:runClient

# Test in-game (NeoForge)
./gradlew :neoforge:runClient
```

### 5. Commit Your Changes

Use conventional commit messages:

```bash
# Format: <type>: <description>
git commit -m "feat: add new time crystal block"
git commit -m "fix: resolve portal spawning issue"
git commit -m "docs: update README installation steps"
```

**Commit types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring
- `test`: Test additions or modifications
- `chore`: Build process or tooling changes

---

## Coding Standards

### Java Code Style

1. **Naming Conventions**:
   - Classes: `PascalCase` (e.g., `TimeGuardianEntity`)
   - Methods/Variables: `camelCase` (e.g., `getCurrentTime`)
   - Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_PORTAL_SIZE`)

2. **Code Organization**:
   - Place common logic in `common/` module
   - Use platform-specific code only when necessary
   - Prefer Architectury APIs over direct loader APIs

3. **Mojang Mappings**:
   - This project uses **Mojang mappings** (not Yarn)
   - Use official Minecraft class names (e.g., `Level`, not `World`)

4. **Comments**:
   - Write comments in English
   - Explain "why", not "what" (code should be self-explanatory)
   - Document non-obvious design decisions

### File Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters max (soft limit)
- **File Encoding**: UTF-8
- **Line Endings**: LF (Unix-style)

### JSON and Data Files

- Use 2-space indentation for JSON files
- Validate JSON syntax before committing
- Follow existing naming conventions for registry IDs

---

## Submitting Changes

### Pull Request Process

1. **Ensure Your Branch is Up-to-Date**:
   ```bash
   git checkout main
   git pull origin main
   git checkout your-branch
   git rebase main
   ```

2. **Push Your Branch**:
   ```bash
   git push origin your-branch
   ```

3. **Create a Pull Request**:
   - Go to [GitHub Pull Requests](https://github.com/ksoichiro/ChronoDawn/pulls)
   - Click "New Pull Request"
   - Select your branch
   - Fill out the PR template with:
     - Clear description of changes
     - Related issue number (e.g., "Fixes #123")
     - Screenshots (if UI changes)
     - Testing notes

4. **Address Review Feedback**:
   - Respond to code review comments
   - Make requested changes
   - Push additional commits to the same branch

### PR Review Criteria

Your PR will be reviewed for:
- ✅ Code quality and readability
- ✅ Test coverage (if applicable)
- ✅ Compatibility with both Fabric and NeoForge
- ✅ No breaking changes to existing features
- ✅ Adherence to coding standards
- ✅ Proper license compliance

---

## Translations

Translations into any language are very welcome — they let players in other regions enjoy Chrono Dawn and make the mod more attractive for modpack creators in those regions. You do **not** need to be a Java developer to contribute a translation.

### Where translations live

Translation files are plain JSON in three source directories:

| Directory | Applies to |
| --- | --- |
| `common/1.20.1/src/main/resources/assets/chronodawn/lang/` | Minecraft 1.20.1 |
| `common/1.21.1/src/main/resources/assets/chronodawn/lang/` | Minecraft 1.21.1 |
| `common/shared-1.21.2+/src/main/resources/assets/chronodawn/lang/` | Minecraft 1.21.2 – 1.21.11 (shared) |

Each directory currently contains `en_us.json` (the reference) and `ja_jp.json` (Japanese). To add a new language, copy `en_us.json` to `<locale>.json` (e.g. `zh_cn.json`, `de_de.json`, `ru_ru.json`) **in all three directories** and translate the values, keeping every key intact.

The full list of valid locale codes is in the [Minecraft Wiki – Language reference](https://minecraft.wiki/w/Language).

### Translation key parity

A small Python script verifies that every locale file shares the same keys as `en_us.json`:

```bash
python3 scripts/check_lang_parity.py
```

It exits with code `1` and prints any missing or extra keys per locale per directory. Run it before opening a PR; PRs that fail this check will be asked for fixes. The script uses only the Python standard library (Python 3.8+).

### Style guidelines

- **Encoding**: UTF-8, no BOM. Do not change line endings (LF).
- **Indentation**: 2 spaces, matching the existing JSON files.
- **Order**: Keep the key order identical to `en_us.json`. This makes diffs reviewable.
- **Blank-line groupers**: The reference file uses blank lines to separate logical sections (blocks, items, entities, etc.). Please preserve them — JSON allows blank lines outside the object body, and translators following en_us key-by-key get them automatically.
- **Proper nouns**: See [`docs/translation-glossary.md`](docs/translation-glossary.md) for guidance on which terms to translate, transliterate, or keep as-is.
- **Format strings**: Preserve `%s`, `%d`, `%1$s`, and similar placeholders exactly. The order may be rearranged using positional indices (`%1$s`, `%2$s`).
- **Don't translate keys** — only the values to the right of the colon.

### Submitting a translation PR

1. Fork the repository and create a branch (e.g. `i18n/zh-cn`).
2. Add or update locale files in **all three** directories listed above. If you only have time for one or two of them, that is still welcome — just say so in the PR description so reviewers can flag the missing files.
3. Run `python3 scripts/check_lang_parity.py` and confirm it exits cleanly.
4. Open a PR. Title format: `i18n: add <Language Name> translation` or `i18n: update <Language Name> translation`.
5. In the PR description, mention which Minecraft versions you have tested, if any. Visual confirmation in-game is appreciated but not required.

### Reviewing and ongoing maintenance

The maintainers cannot personally vet quality in every language, so accepted translations are the responsibility of the contributing community. If you are a native or fluent speaker of an existing translation and notice issues, PRs that improve translations are welcome under the same flow.

When new English keys are added by code changes, existing translations will start failing the parity check until updated. The maintainers may merge code changes with stale translations rather than blocking — translators are then encouraged to follow up with PRs filling the new keys.

---

## License

### LGPL-3.0 Compliance

Chrono Dawn is licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**.

**What this means for contributors**:

1. **All contributions** are licensed under LGPL-3.0
2. **Derivative works** must remain LGPL-3.0 (copyleft requirement)
3. **Dependencies**: Only use LGPL-3.0-compatible licenses (MIT, Apache 2.0, BSD)
4. **Avoid incompatible licenses**: Do not add dependencies with GPL-incompatible licenses (CC-BY-NC, proprietary)

### Adding Dependencies

Before adding a new dependency:

1. Verify the dependency's license is LGPL-3.0 compatible
2. Document the dependency in `THIRD_PARTY_LICENSES.md`
3. Add dependency information to `build.gradle`

### License Headers (Optional)

You may optionally add LGPL-3.0 headers to new source files:

```java
/*
 * Chrono Dawn - A Time-Manipulation Dimension Mod for Minecraft
 * Copyright (C) 2025 Soichiro Kashima
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
```

See [LICENSE](LICENSE) for the complete license text.

---

## Additional Resources

- **Developer Guide**: [docs/developer_guide.md](docs/developer_guide.md) - Comprehensive technical documentation
- **Player Guide**: [docs/player_guide.md](docs/player_guide.md) - Gameplay information
- **Design Spec**: [specs/chrono-dawn-mod/spec.md](specs/chrono-dawn-mod/spec.md) - Feature specifications
- **GitHub Issues**: [Issue Tracker](https://github.com/ksoichiro/ChronoDawn/issues)

---

## Questions?

If you have questions about contributing:

1. Check the [Developer Guide](docs/developer_guide.md)
2. Search [existing issues](https://github.com/ksoichiro/ChronoDawn/issues)
3. Open a new issue with your question

Thank you for contributing to Chrono Dawn! 🕰️
