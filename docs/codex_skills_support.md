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

## Hooks (PR gate)

Research date: 2026-11-27, from [Hooks](https://learn.chatgpt.com/docs/hooks)
(`developers.openai.com/codex/hooks` redirects there). The page names no
minimum Codex version. Re-check it before relying on the details below.

- Codex has `PreToolUse` hooks that can deny a tool call. They are on by
  default (`[features] hooks = false` turns them off).
- Project hooks go in `.codex/hooks.json` (or `[hooks]` tables in
  `.codex/config.toml`) and load only from a trusted project. In a linked Git
  worktree a project layer may read hooks from the main checkout.
- A new or changed hook is skipped until the user reviews and trusts it in
  `/hooks`. The trust is stored against the hook's hash, so a changed command
  needs a new review.
- `PreToolUse` gets JSON on stdin with `tool_name`, `tool_input.command`,
  `cwd` and extra fields (`turn_id`, `permission_mode`, ...). Shell commands
  use `tool_name: "Bash"`. A deny is exit code 2 with the reason on stderr, or
  `{"hookSpecificOutput": {"hookEventName": "PreToolUse", "permissionDecision":
  "deny", "permissionDecisionReason": "..."}}` on stdout. This is the shape
  `scripts/hooks/claude_pr_gate.py` already reads and writes, so the same
  script serves both agents without a conversion layer.
- No project-directory variable is documented. The hook command finds the
  root with `git rev-parse --show-toplevel`.
- Unsupported output (`ask`, `continue: false`) and a crashing hook fail open.
  The page calls tool hooks "a useful guardrail, not a complete enforcement
  boundary". Hosted tools are not covered.
- Not confirmed from the page: whether `PreToolUse` fires for subagent tool
  calls, in `codex exec`, or under the auto-review and full-auto modes.
  `--dangerously-bypass-hook-trust` skips only the trust review.

### Decision

The Codex side runs the same gate. `.codex/hooks.json` calls
`scripts/hooks/claude_pr_gate.py` for `Bash`, with no change to the checks.
Moving the checks to CI would not cover the independent verification, because
`build/verification/` exists only in the checkout that ran the review, and
documentation alone was skipped before the hook existed. `.codex/**` is tier D
in `scripts/merge_tier/rules.json`.

The owner has to trust the hook once per checkout and after each change to its
command (`/hooks` in the Codex CLI). Until then Codex skips it, so the
instructions in `AGENTS.md` stay the fallback. The hook, the trust step and
the three open points above are verified by running Codex, which this
repository's tests do not do.
