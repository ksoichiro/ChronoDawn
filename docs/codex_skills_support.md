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
- `pr-gate` says that Claude Code runs the hook from `.claude/settings.json`
  and Codex from `.codex/hooks.json`, and that Codex must run the checks by
  hand until the hook is trusted (see "Hooks (PR gate)" below).

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
- The page does not say whether `PreToolUse` fires for subagent tool calls, in
  `codex exec`, or under the auto-review and full-auto modes. The runtime check
  below covers them.
- `--dangerously-bypass-hook-trust` skips only the trust review.

### Decision

The Codex side runs the same gate. `.codex/hooks.json` calls
`scripts/hooks/claude_pr_gate.py` for `Bash`, with no change to the checks.
Moving the checks to CI would not cover the independent verification, because
`build/verification/` exists only in the checkout that ran the review, and
documentation alone was skipped before the hook existed. `.codex/**` is tier D
in `scripts/merge_tier/rules.json`.

The owner has to trust the hook once per checkout and after each change to its
command (`/hooks` in the Codex CLI). Until then Codex skips it, so the
instructions in `AGENTS.md` stay the fallback.

### Runtime check (2026-11-27)

The owner ran `git push origin codex-gate-trial` on a throwaway branch that
changed `TimeCompassItem.java` without a test, in a Codex session after
trusting the hook. Codex denied it before running, with the gate's reasons (no
test or scenario, no `build/verification/diff.patch`). The same denial
happened in each of these cases:

- a normal clone,
- a linked worktree,
- `codex exec`,
- auto-review (`--approve-for-me`),
- no approvals (`--ask-for-approval never`, `--yolo`),
- a subagent running the command.

The Codex version is not recorded. Before the hook file was on `main`, `/hooks`
showed nothing in a linked worktree, so a worktree may need the file on the
main checkout. Tool paths that the Codex page says can
opt out of hooks are not covered. Treat the gate as a guardrail, as the
Codex page does. The `Merge tier` CI check stays the floor.
