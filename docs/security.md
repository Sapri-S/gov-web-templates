# Security

This document lists the security controls the template provides, who is responsible for each part, and the evidence to collect for each client. Control families follow NIST SP 800-53 Rev. 5 so the same evidence can support a GovRAMP or FedRAMP review later.

## Who is responsible for what

| Area | Hosting provider | Us (template and delivery) | Client |
| --- | --- | --- | --- |
| Data center, hardware, network | ✔ | | |
| Operating system and container runtime patching | ✔ (managed services) or us (own VM) | ✔ when we run the VM | |
| TLS certificates and web application firewall | Configured on hosting | ✔ set up and monitor | Approves domain and DNS |
| Application code and dependencies | | ✔ | |
| Staff accounts and MFA | | Connects the identity provider | ✔ owns accounts, approves access |
| Data retention and public records | | Implements the rules | ✔ decides the rules |
| Incident decisions and public notices | | ✔ technical response | ✔ decisions and notices |

[CLIENT] Adjust this table to the client's contract and hosting.

## Controls the template provides

| NIST family | What the template does | Where |
| --- | --- | --- |
| Access control (AC) | Public endpoints are limited to submit and look up. Staff endpoints need a token with the `portal.staff` scope. Every other route is denied. | `SecurityConfig.java` |
| Identification and authentication (IA) | Staff sign in through the client's OpenID Connect provider, where MFA is enforced. The API keeps no passwords. | `application.yml` (`OIDC_ISSUER_URI`) |
| Audit and accountability (AU) | Every submission and status change writes an audit event in the same transaction, with actor and time. No personal data in audit details. | `audit/` |
| System and communications protection (SC) | HTTPS only (HSTS), strict Content-Security-Policy, frame blocking, no-sniff, no referrer leakage, 64 KB request limit at the proxy | `SecurityConfig.java`, `deploy/nginx.conf` |
| System and information integrity (SI) | Server-side validation of every field, allow-listed categories and form fields, unknown fields rejected, spam trap, no stack traces in responses | `ServiceRequestService.java`, `FormValidator.java` |
| Configuration management (CM) | Schema changes only through versioned migrations; database schema validated on startup; secrets only from environment variables | `db/migration/`, `deploy/.env.example` |
| Risk assessment (RA) | Weekly dependency update pull requests; build fails on high or critical npm vulnerabilities | `.github/dependabot.yml`, `ci.yml` |
| Privacy | Public status lookups never return contact details; tracking numbers are random, not sequential | `ServiceRequestDtos.java` |

## What each client project must add

- [ ] Choose hosting and record it in `docs/client-intake.md`.
- [ ] Put a web application firewall and rate limiting in front of the public endpoints.
- [ ] Connect the identity provider, require MFA for staff, and give the `portal.staff` scope only to approved staff.
- [ ] Use a dedicated database role. Grant it only `INSERT` and `SELECT` on `audit_event`.
- [ ] Encrypt the database and backups at rest (hosting setting).
- [ ] Set the retention period for submissions and audit events, and schedule deletion.
- [ ] Send API and proxy logs to central storage the application can't modify.
- [ ] Turn on Dependabot alerts and secret scanning in the GitHub repository settings.
- [ ] Run a vulnerability scan before launch and monthly after.
- [ ] [CLIENT] Add any controls the contract or the client's IT security policy requires.

## Evidence checklist

Collect these per client and per release. Store them in the client's project folder, not in this template.

| Evidence | How to produce it | How often |
| --- | --- | --- |
| Passing CI run (tests, accessibility tests, dependency audit) | GitHub Actions run link | Every release |
| Dependency update history | Merged Dependabot pull requests | Monthly |
| Vulnerability scan report | Scanner run against production | Before launch, then monthly |
| Staff access list and review | Export from identity provider, signed by client | Quarterly |
| Audit log sample | Query of `audit_event` for a date range | Quarterly |
| Backup restore test | Steps and result from `docs/operations.md` | Quarterly |
| Security headers check | Response headers from production | Every release |
| Incident exercise | Notes from a tabletop run of `docs/incident-response.md` | Yearly |
