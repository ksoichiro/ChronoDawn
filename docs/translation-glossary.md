# Translation Glossary

This document gives translators guidance on how to handle Chrono Dawn's recurring proper nouns and themed vocabulary. It is a starting reference, not a binding rule book — when in doubt, prioritise what reads naturally in the target language.

## General principles

Chrono Dawn uses three kinds of names. They should usually be handled differently:

| Name type | Examples (English) | How to translate |
| --- | --- | --- |
| **Coined / fantasy proper nouns** | Chronoblade, Floq, Paradox Crawler, Chrono Dawn (the dimension) | Treat as a proper noun. Transliterate phonetically rather than translate the meaning. The mod's identity should survive across languages. |
| **Descriptive compounds** | Time Wood, Time Tyrant, Reversing Time Sandstone, Temporal Stone | Translate the meaning into natural target-language equivalents. These names exist to describe what the thing is. |
| **Standard Minecraft vocabulary** | block, ore, sword, helmet, dimension, advancement | Match the official Minecraft translation in the target language so the mod feels native to existing players. |

When a name is on the boundary (e.g. "Clockstone" — coined word that also reads like "clock + stone"), look at the existing `ja_jp.json` entry for guidance on the convention chosen for ChronoDawn (Clockstone is transliterated as クロックストーン).

## Reference table (English → Japanese examples)

These are taken from the existing `ja_jp.json` translation. They illustrate the conventions above, and can serve as a model when picking strategies for other languages.

| English | Japanese | Strategy |
| --- | --- | --- |
| Chrono Dawn (dimension / mod) | クロノドーン | Transliterate (proper noun) |
| Time Tyrant | 時間の暴君 | Translate meaning |
| Time Guardian | 時の番人 | Translate meaning |
| Chronos Warden | クロノスの監視者 | Mixed — proper noun + translated role |
| Chronoblade | クロノブレード | Transliterate (coined item) |
| Time Wood | 時の木 | Translate meaning |
| Clockstone | クロックストーン | Transliterate (coined material) |
| Enhanced Clockstone | 強化クロックストーン | Translate the modifier, keep transliterated base |
| Temporal Amber | 時琥珀 | Translate meaning |
| Reversing Time Sandstone | 逆流の砂岩 | Translate meaning |
| Chrono Aegis | クロノスの盾 | Mixed — proper noun stem + translated noun |
| Paradox Crawler | パラドックス・クローラー | Transliterate (mob name) |
| Floq | フロック | Transliterate (coined mob name) |
| Master Clock (final dungeon) | マスタークロック | Transliterate (proper noun for a structure) |

## Things to keep verbatim

- **Format placeholders** like `%s`, `%d`, `%1$s`, `%2$d` — never translate, never add or remove. You may reorder using positional indices (`%1$s`).
- **Lang keys** (the part to the left of the colon) — these are identifiers used by Minecraft and the mod's code. Only the value strings on the right are translated.
- **Tag-like substrings inside values**, e.g. anything in backticks or capitalised mod IDs (`chronodawn:something`).

## Cultural notes

- **Boss / mob names with cultural connotation**: a name like "Time Tyrant" carries dramatic weight; aim for an equivalently dramatic noun in the target language rather than a literal word-for-word translation.
- **Themed adjectives ("Temporal", "Chrono-", "Time-")**: in English these are roughly interchangeable. In your language they may collapse into one prefix or split differently — that is fine. Internal consistency within one language is more important than mirroring the English distinction.
- **Tooltip and message strings** (keys starting with `*.tooltip.*`, `message.chronodawn.*`): these are read by players in flow, so favour readable, natural sentences over literal renderings.

## When in doubt

1. Look at the existing `ja_jp.json` entry for the same string — even if you don't read Japanese, the glossary above shows which strategy was used.
2. If the term has not appeared before, pick the strategy that matches its category in the table at the top.
3. Add a brief note in your PR description if you made a deliberate departure from these guidelines, so reviewers can confirm it is intentional.
