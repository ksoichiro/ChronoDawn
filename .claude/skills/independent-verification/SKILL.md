---
name: independent-verification
description: Use after implementing a change and before opening its pull request in Chrono Dawn. Runs the independent verification of docs/merge_policy.md - a fresh subagent that only sees the diff, the task text and the tier tries to break the change, only reproduced findings count, and the result goes into the PR body's Verification section.
---

# Independent verification

`docs/merge_policy.md` ("Independent verification") is the policy. This skill
is the procedure. Run it once the change is committed and before
`gh pr create`. Run it again after a push that changes behavior.

## 1. Fix the inputs

The reviewer gets exactly three things. Nothing from this conversation.

1. **Diff.** Write it to a git-ignored path in the checkout, not `$TMPDIR`
   (the sandbox resolves `$TMPDIR` differently per command):
   ```bash
   mkdir -p build/verification
   git diff "$(git merge-base origin/main HEAD)" HEAD > build/verification/diff.patch
   ```
2. **Task text.** The requirement as the owner wrote it (the task body and
   any comment that changed the requirement). Copy it verbatim, do not
   summarize it and do not add how you solved it.
3. **Tier.** The output of the classifier, raised by hand only when the
   content calls for it (see "Classifying a diff by its files"):
   ```bash
   python3 scripts/merge_tier/classify.py
   ```

Do not pass your own test results, your reasoning, the files you think are
risky, or a list of things you already checked. Handing the reviewer your
hypothesis makes it confirm the hypothesis.

## 2. Dispatch the reviewer

Use the Agent tool with the default general-purpose agent. Never use `fork`,
which inherits this conversation. Do not pass a `model` override: the policy
fixes the reviewer to the main agent's model. Run it in the foreground, since the next
step depends on its result. Fill the template below and send it as the
prompt.

```text
You are reviewing a change to Chrono Dawn, a multi-loader, multi-version
Minecraft mod, in the checkout at <absolute path of this checkout>. Your job
is to break it, not to approve it.

Task (as written by the owner):
<task text>

Merge tier: <tier> (definitions in docs/merge_policy.md)

The diff is in build/verification/diff.patch. It is already committed on the
current branch, so the checkout contains it.

Do this:
1. Read the diff, CLAUDE.md, docs/merge_policy.md and the rows of
   docs/bug_prevention.md that match the touched files.
2. Look for ways the change fails: edge cases, the other loader (Fabric,
   NeoForge, Forge on 1.20.1), the other Minecraft versions, existing worlds
   and packs, and the bug classes from docs/bug_prevention.md.
3. Try to reproduce each suspicion by running something: a build, a unit
   test, a validator, a GameTest, a scenario, a small script. Follow
   CLAUDE.md for commands, parallel-verification limits and the sandbox.
   Do not edit tracked files. Scratch files go under build/verification/.
4. Report only reproduced findings. A suspicion you could not reproduce is
   dropped, not reported.

Answer in this form, in English:

Commands run:
- `<command>`: <result in a few words>

Reproduced findings:
1. <what fails> - reproduced with `<command>`: <the failing output, log line
   or test name>

If nothing reproduced, write "No reproduced findings" under the heading and
keep the list of commands, so the reader can see what was tried.
```

## 3. Handle the findings

- A finding without a command and its output is not a finding. Drop it, even
  if it sounds right.
- Fix each reproduced finding, then re-run the reviewer's command and confirm
  it passes now.
- If you do not fix one, write the reason in the PR body. Do not drop it
  silently.
- A fix that changes behavior means a new diff. Run the classifier again, and
  run the reviewer again when the fix is more than a one-line correction.

## 4. Record it in the PR body

Add this section to the pull request body. Keep `Merge tier:` on its own
line: the `Merge tier` CI check reads it and fails when it is lower than the
diff's tier.

```markdown
## Verification

Merge tier: <A|B|C|D>

Independent reviewer (fresh subagent, same model, diff + task + tier only):
- `<command>`: <result>
- `<command>`: <result>

Reproduced findings:
1. <finding>. Fixed in <commit>, `<command>` passes now.
2. <finding>. Not fixed: <reason>.
```

Write "No reproduced findings." when the list is empty, and keep the command
list. A tier A pull request may be auto-merged only with this section present
and no open finding (see "Auto-merge" in `docs/merge_policy.md`).
