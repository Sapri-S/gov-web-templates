# Government web templates

Reusable starting points for local government websites and resident service applications. Each client project copies this repository and fills in the parts marked `[CLIENT]`, the way a class extends an abstract base class.

## Scope

**Included now:** forms and service requests (submit, track status, staff review), a WordPress starter for content sites, and the security, accessibility and operations documents that go with them.

**Not included yet:** payments, agendas and meetings, maps, resident accounts and translation. These will be added as modules that connect to outside products.

## Repository layout

| File or folder | Purpose |
| --- | --- |
| `README.md` | Scope, setup and supported configurations |
| `apps/` and `wordpress/` | Portal example (Spring Boot API + React frontend) and WordPress starter |
| `docs/architecture.md` | Components, data flows and integrations |
| `docs/security.md` | Controls, responsibilities and evidence checklist |
| `docs/accessibility.md` | Standards, testing process and results |
| `docs/operations.md` | Deployment, monitoring, updates, backup and restore |
| `docs/incident-response.md` | Response steps and escalation contacts |
| `docs/client-intake.md` | Customer requirements, data and hosting decisions |
| `deploy/` and `.github/workflows/` | Deployment configuration and automated checks |

## Supported configurations

| Area | Supported now | Notes |
| --- | --- | --- |
| Backend | Spring Boot 3.5, Java 21 | Business rules live only in the API |
| Database | PostgreSQL 17 | Migrations run with Flyway on startup |
| Frontend | React 19 with USWDS 3 | Design system is isolated in `apps/portal-web/src/ui/` |
| Design system | Client's own, else USWDS (local and federal), MDWDS (Maryland state) | See "Design system" in `docs/architecture.md` |
| Staff sign-in | Any OpenID Connect provider | Tokens must carry the `portal.staff` scope |
| Content sites | WordPress 6.6+ on PHP 8.1+ | Block theme plus hardening plugin |
| Hosting | Commercial hosting first (single server with Docker Compose) | FedRAMP and GovRAMP platforms come later |

## Quick start

**API** (needs Java 21, Maven and a PostgreSQL database):

```bash
cd apps/portal-api
export DATABASE_URL=jdbc:postgresql://localhost:5432/portal DATABASE_USERNAME=portal DATABASE_PASSWORD=...
export OIDC_ISSUER_URI=https://your-identity-provider.example/   # required; used for staff endpoints
mvn spring-boot:run
```

Run the tests with `mvn verify`. They use an in-memory database and mock sign-in, so they need neither.

**Frontend** (needs Node 22):

```bash
cd apps/portal-web
npm ci
npm run dev      # http://localhost:5173, forwards /api to localhost:8080
npm test         # includes automated accessibility checks
```

**Everything on one server:** see `docs/operations.md`.

**WordPress:** see `wordpress/README.md`.

## Start a client project

1. Fill in `docs/client-intake.md` with the client.
2. Create a private repository from this one.
3. Add `apps/portal-api/src/main/resources/application-<client>.yml` with the client's request categories and forms.
4. Update `apps/portal-web/src/config/site.ts` and `src/theme/tokens.css`.
5. If the client has its own design system, rewrite `apps/portal-web/src/ui/` only.
6. Fill in the `[CLIENT]` items in `docs/security.md`, `docs/operations.md` and `docs/incident-response.md`.

## Status

The frontend and WordPress starter are built and checked. The Spring Boot API and its tests were written without being compiled, because the workspace they were written in couldn't download Java libraries. The CI workflow compiles and tests the API on the first push; fix anything it reports before using the API for a client.
