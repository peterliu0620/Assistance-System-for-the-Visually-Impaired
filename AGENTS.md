# Repository Guidelines

## Project Structure & Module Organization
All paths in this document are relative to the repository root. If the runtime `cwd`, workspace path, or sandbox context differs from the documented example path, verify the actual repository root first and then resolve module paths from that root before running commands.

This repository is split into four application modules:
- `admin/backend/`: Spring Boot admin API service.
- `admin/frontend/`: admin frontend.
- `user/backend/`: Spring Boot user API service. Core Java code is in `user/backend/src/main/java/com/example/demo`, configuration is in `user/backend/src/main/resources/application.properties`, and tests are in `user/backend/src/test/java/com/example/demo`.
- `user/frontend/`: uni-app Vue client. Pages live in `user/frontend/pages/` (current entry: `user/frontend/pages/index/index.vue`), shared assets are in `user/frontend/static/`, and app-level config files are `user/frontend/pages.json`, `user/frontend/manifest.json`, and `user/frontend/uni.scss`.

Treat generated output such as `admin/backend/target/`, `user/backend/target/`, `admin/frontend/unpackage/`, `user/frontend/unpackage/`, and `user/frontend/dist/` as build artifacts; do not edit them manually.

## Build, Test, and Development Commands
- `cd user/backend && mvn spring-boot:run`: start the user backend locally.
- `cd user/backend && mvn test`: run user backend JUnit tests.
- `cd user/backend && mvn clean package`: build the user backend artifact.
- `cd user/frontend && npm install && npm run dev:h5`: run the user frontend in H5 mode (as documented in `README.md`).

If frontend npm scripts are not available in your local setup, run the uni-app project through your IDE workflow and keep output behavior consistent with `dev:h5`.

## Coding Style & Naming Conventions
- Java: use 4-space indentation, `PascalCase` class names, `camelCase` methods/fields, and lowercase package names (for example `com.example.demo.controller`).
- Keep REST controllers under `.../controller` and route paths under `/api/*`.
- Vue/uni-app: follow existing tab-indented `.vue` style, keep page directories lowercase (for example `pages/index`), and keep static assets under `user/frontend/static`.

## Testing Guidelines
- Backend testing uses JUnit 5 with Spring Boot Test (`spring-boot-starter-test`).
- Place tests in mirrored package paths under `user/backend/src/test/java`.
- Name test classes with a `*Tests` suffix (example: `DemoApplicationTests`).
- Run `mvn test` before pushing backend changes.

No automated frontend test suite is configured currently; include a manual smoke-test note for UI changes.

## Commit & Pull Request Guidelines
Current history uses short messages (examples: `初始化模版`, `first commit`). Keep commits concise and imperative; prefer `<area>: <summary>` when possible (example: `backend: add hello endpoint validation`).

## Agent Commit Workflow
- In this repository environment, if local read commands are likely to fail under sandbox restrictions, request escalation on the first necessary command instead of retrying without escalation.
- Commit only files related to the current task. Do not include unrelated changes, generated files, or user edits that were not part of the request.
- If the working tree already contains user changes or unexpected modifications that are not clearly part of the current task, stop and ask before committing.
- Commit messages must be written in Chinese.
- Use concise imperative commit messages, preferably in the format `<area>: <summary>`.

For PRs, include:
- What changed and why.
- Which module(s) were touched (`backend`, `frontend`, or both).
- Verification performed (commands run, plus screenshots for UI changes).
- Linked issue/task and any config changes.
