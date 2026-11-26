# Using the project skills from Codex

Research date: 2026-11-24. Re-check the Codex documentation before acting on
this, since its skill paths have changed before.

## Current state

- Codex reads `AGENTS.md`, so the shared rules there (including the
  "Verification Reporting" rules before a PR) already apply to Codex.
- The 20 project skills live in `.claude/skills/<name>/SKILL.md`. Codex does
  not scan `.claude/`. `.agents/skills` is a symlink to `../.claude/skills`
  (option 1 below), which is where Codex looks.
- The "read the matching skill before the work" rule and the skill list are in
  `AGENTS.md` ("Project Skills"), so Codex sees them too.
- Every `SKILL.md` already has the `name` and `description` frontmatter that
  Codex requires.

## What Codex supports

From [Build skills](https://learn.chatgpt.com/docs/build-skills) (the old
`developers.openai.com/codex/skills` URLs redirect there):

- A skill is a directory with a `SKILL.md` that has `name` and `description`
  frontmatter, plus optional scripts and references. The format is the same as
  Claude Code's.
- Repository skills are read from `.agents/skills` in every directory from the
  current working directory up to the repository root. User skills are in
  `$HOME/.agents/skills`.
- "Codex supports symlinked skill folders and follows the symlink target when
  scanning these locations."
- Codex picks a skill implicitly when the task matches its `description`, or
  explicitly through `/skills` or a `$skill-name` mention.
- The initial skill list takes at most 2% of the context window (8,000
  characters when the window is unknown, adjustable with
  `skills.max_context_tokens`), and Codex shortens descriptions first when
  many skills are installed.
- Standalone repository skills work in Codex CLI, the IDE extension and the
  ChatGPT desktop app. Chat and Work on web and mobile only use skills
  bundled in plugins.
- Some search results still show `.codex/skills` as the repository path. The
  current page lists only `.agents/skills` for skills, and `.codex/` only for
  configuration.

## Claude Code specific content in the skills

Reworded so that they hold for Codex as well:

- `independent-verification` has separate steps for Claude Code (Agent tool,
  no `fork`) and Codex (`collaboration.spawn_agent` with `fork_turns="none"`).
- `verification-commands` says "without the agent sandbox" and names the
  Claude Code and Codex cases. It also notes that the PostToolUse hook is
  Claude Code only and tells Codex to run `validateVersionSync` by hand.
- `pr-gate` says the hook is Claude Code only and that Codex must do the same
  checks by hand.

## Decision (2026-11-27)

Option 1: `.agents/skills` is a relative symlink to `../.claude/skills`.

- One copy of each skill, and description-based selection and `$skill-name`
  both work.
- `.claude/skills` stays the real directory, so the Claude Code sandbox rule
  that keeps it read-only is unchanged (option 2 would change it).
- Merge tier: `.agents/**` is already in tier D (`scripts/merge_tier/rules.json`),
  so no rule change was needed.
- Re-checked the Codex documentation on 2026-11-27: repository skills are
  still read from `.agents/skills`, symlinked skill folders are followed, and
  the skill list is limited to 2% of the context window.
- Not verified here: a Codex session listing all 20 skills and any shortening
  of descriptions by the context budget. Check with `/skills` in Codex, and if
  descriptions are cut, shorten them or set `skills.max_context_tokens`.
- Windows checkouts may not create the symlink. If that is reported, switch
  to option 2 or 3.

## Options

1. Symlink `.agents/skills` to `.claude/skills` (adopted).
2. Move the skills to `.agents/skills` and symlink `.claude/skills` to it.
   Makes the neutral location the source, but changes the sandbox assumption.
3. Add a skill index to `AGENTS.md` with the `SKILL.md` paths. Works for any
   agent, but skills are no longer picked by description. The index has to
   cover all 20 skills.
