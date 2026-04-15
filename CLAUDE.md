# CLAUDE.md

This file adds project-level behavioral guidance for coding agents in this repository.

Priority:
- Follow repository-specific instructions in `AGENTS.md` first.
- Use this file to guide implementation style and decision-making.
- If the two conflict, `AGENTS.md` wins.

Repository context:
- The current repository is split into four application modules:
  - `admin/backend`: Spring Boot admin service
  - `admin/frontend`: admin frontend
  - `user/backend`: Spring Boot user service
  - `user/frontend`: user frontend
- Generated output such as `target/` and `unpackage/` should not be edited manually.
- Backend database migrations live under each backend module's `src/main/resources/db/migration`.

Behavioral guidelines:

## 1. Think Before Coding
- Do not assume unclear requirements.
- State important assumptions before implementing when they affect behavior or scope.
- If multiple interpretations are plausible, choose the safest one and say what you assumed, or ask when the risk is high.
- Prefer concrete verification targets over vague goals.

## 2. Simplicity First
- Implement the minimum change that solves the requested problem.
- Do not add speculative abstractions, configuration, or future-proofing that was not requested.
- Match the existing structure and conventions of the module you are editing.
- If a simpler solution is available, take it.

## 3. Surgical Changes
- Change only files and code paths that are directly relevant to the task.
- Do not refactor unrelated code, reformat broad areas, or clean up incidental issues unless asked.
- Remove only the unused code or imports created by your own change.
- If you notice unrelated problems, mention them instead of silently fixing them.

## 4. Goal-Driven Execution
- Define what success looks like before making substantial changes.
- For bug fixes, prefer reproducing the bug with a test or another concrete verification step before fixing it.
- For features, verify the exact requested behavior with the narrowest practical check.
- After changes, run relevant validation for the touched module when feasible.

Working rules for this repository:
- Resolve all paths from the actual repository root before running commands.
- Keep backend work within the relevant module package structure and route conventions already used there.
- Keep frontend work aligned with the existing uni-app or frontend module style instead of imposing a new pattern.
- Treat commit workflow, testing expectations, and escalation rules in `AGENTS.md` as authoritative.
