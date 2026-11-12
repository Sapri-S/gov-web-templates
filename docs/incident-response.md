# Incident response

Use these steps when there's a suspected security incident, data exposure or major outage. Fill in the contacts before launch, and practice the steps once a year.

## Escalation contacts

| Role | Name | Phone | Email | Backup |
| --- | --- | --- | --- | --- |
| Our incident lead | [CLIENT] | | | |
| Client project owner | [CLIENT] | | | |
| Client IT security contact | [CLIENT] | | | |
| Client legal or privacy contact | [CLIENT] | | | |
| Client communications contact | [CLIENT] | | | |
| Hosting provider support | [CLIENT] | | | |
| Identity provider support | [CLIENT] | | | |

## Severity

| Level | Examples | Notify the client |
| --- | --- | --- |
| 1, Critical | Resident data exposed or stolen; site defaced; attacker has access | Immediately, by phone |
| 2, High | Suspected but unconfirmed exposure; service down for residents | Within 1 hour |
| 3, Moderate | Vulnerability found but not exploited; partial outage | Same business day |
| 4, Low | Blocked attack attempts; minor bug with no data risk | In the regular report |

[CLIENT] Replace these times with the ones in the contract.

## Steps

1. **Detect and record.** Write down what was seen, when, and by whom. Start a timeline and keep it updated.
2. **Assess severity** using the table above, and notify the client contacts in the order listed.
3. **Contain.** For example: take the affected service offline, block the attacking addresses at the firewall, revoke compromised staff access or tokens, rotate secrets. Keep evidence: don't wipe servers or delete logs.
4. **Preserve evidence.** Copy relevant logs, the audit table and system snapshots to secure storage before changing anything else.
5. **Eradicate.** Fix the cause (patch, configuration change, removed account) and confirm it's gone.
6. **Recover.** Restore service from known-good code and, if needed, backups. Watch closely for repeat activity.
7. **Notify.** The client decides on notices to residents, regulators and the public, with its legal counsel, under the applicable breach notification laws and contract terms. We supply the technical facts.
8. **Review.** Within 10 business days, write a short report: what happened, impact, timeline, root cause and fixes. Add follow-up tasks to the project backlog.

## Quick actions

| Need | Action |
| --- | --- |
| Take the portal offline | `docker compose -f deploy/docker-compose.yml stop web` and show a maintenance page at the proxy |
| Remove a staff member's access | Disable them in the identity provider, or remove the `portal.staff` scope |
| Rotate the database password | Change it in PostgreSQL and `deploy/.env`, then restart the API |
| Find what a staff member changed | `SELECT * FROM audit_event WHERE actor = '<subject>' ORDER BY occurred_at;` |
