# Using the project skills from Codex

Research date: 2026-11-24. Re-check the Codex documentation before acting on
this, since its skill paths have changed before.

## Current state

- Codex reads `AGENTS.md`, so the shared rules there (including the
  "Verification Reporting" rules before a PR) already apply to Codex.
- The 19 project skills live in `.claude/skills/<name>/SKILL.md`. Codex does
  not scan `.claude/`, so none of them is available to Codex today.
- The "read the matching skill before the work" rule and its skill list are
  only in `CLAUDE.md`, which Codex does not read. That list names 7 of the 19
  skills. `AGENTS.md` names one skill (`verification-commands`) without a
  path, so Codex cannot open it.
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

- `independent-verification` tells the agent to use "the Agent tool with the
  default general-purpose agent" and never `fork`. These are Claude Code
  terms. Codex needs its own way to start a reviewer that does not inherit the
  conversation, or the step has to be written without naming a tool.
- `verification-commands` says some tasks must run outside the sandbox. The
  notes were written for the Claude Code sandbox, and Codex has its own, so
  they may not match.
- The skill list in `CLAUDE.md` ("Read the matching skill before the work")
  has no counterpart in `AGENTS.md`.

## Options

1. Symlink `.agents/skills` to `.claude/skills`. One copy of every skill, no
   duplication. Needs a check that the symlink works on the Codex version in
   use and on Windows checkouts, which may not create symlinks.
2. Move the skills to `.agents/skills` and symlink `.claude/skills` to it.
   Same effect, but makes the neutral location the source.
3. Add a skill index to `AGENTS.md` with the `SKILL.md` paths, and tell agents
   to read the matching file before the work. Works for any agent without
   skill support, but skills are no longer picked by description. The index
   has to cover all 19 skills, not only the 7 that `CLAUDE.md` lists.

Any option that adds `.agents/` also needs `.agents/**` in the tier D pattern
of `scripts/merge_tier/rules.json` and the table in `docs/merge_policy.md`,
because those paths are agent instructions. With the current rules only
`.agents/skills/*/SKILL.md` is tier D (through `**/SKILL.md`). The
`.agents/skills` symlink itself and files under a skill's `scripts/` fall to
the default tier C, and Markdown under `references/` to tier A.
