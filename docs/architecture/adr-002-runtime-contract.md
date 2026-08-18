<!-- markdownlint-disable MD013 -->
# ADR: Repository Execution Boundary and Runtime Contract

## Context

To ensure parallel development and autonomous task execution, the repository structure, runtime commands, and backend/frontend boundaries must be explicitly defined before feature implementation begins. The presence of `pom.xml` and `frontend/package.json` indicates that the foundational manifests are established.

## Decision

We establish the following fixed service boundaries and local runtime contract:

**Backend Boundary (Java/Spring Boot)**
*   **Root:** Repository root (`/`)
*   **Source Code Location:** `src/main/java/` and `src/test/java/`
*   **Install Command:** `mvn install -DskipTests`
*   **Run Command:** `mvn spring-boot:run`
*   **Test Command:** `mvn test`
*   **Rule:** Backend code must remain isolated in the root Maven project.

**Frontend Boundary (Node/Svelte/Vite)**
*   **Root:** `frontend/` directory
*   **Source Code Location:** `frontend/src/`
*   **Install Command:** `npm ci` (executed within `frontend/`)
*   **Run Command:** `npm run dev` (executed within `frontend/`)
*   **Test Command:** `npm run test` (executed within `frontend/`)
*   **Rule:** All UI components, styles, and frontend logic must be placed exclusively under the `frontend/` directory.

## Rationale

*   **Bounded Contexts:** Physically separating the backend and frontend into distinct directories (`src/` vs `frontend/`) enforces strict isolation of concerns, adhering to topological boundary principles (Achille Varzi).
*   **Autonomy:** Providing clear install, run, and test commands allows automated agents (Jules) to operate without human intervention, fulfilling the stated JTBD.

## Handoff

**Next Owner Role:** BARCAN-TAG-09
