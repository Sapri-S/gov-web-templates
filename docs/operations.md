# Operations

The default setup runs the database, API and web container on one server at a commercial host, behind a TLS proxy or CDN with a web application firewall. Larger clients or FedRAMP and GovRAMP requirements need a different hosting plan, recorded in `docs/client-intake.md`.

[CLIENT] Hosting provider: ______ · Server or service: ______ · Domain: ______ · Owner of the hosting account: ______

## Environments

| Environment | Purpose | Data |
| --- | --- | --- |
| Local | Development | Fake data only |
| Staging | Client review and testing before release | Fake or scrubbed data |
| Production | Live site | Real resident data |

Never copy production data into local or staging environments.

## Deploy

1. Make sure CI passed on the commit you're releasing.
2. On the server, update the code to that commit.
3. Check `deploy/.env` has every value from `deploy/.env.example`.
4. Run:

   ```bash
   docker compose -f deploy/docker-compose.yml --env-file deploy/.env up -d --build
   ```

5. Check that `https://<site>/healthz` returns `{"status":"UP"}`, then complete one test submission on staging.
6. Record the release (date, commit, who deployed) in the client's release log.

Database changes run automatically from `apps/portal-api/src/main/resources/db/migration` when the API starts. Never edit a migration that has already run; add a new one.

**Rollback:** redeploy the previous commit. If a migration changed the schema, restore the pre-release backup first (see below).

## Monitoring

| What | How | Alert when |
| --- | --- | --- |
| Uptime | External check on the home page and `/healthz` every minute | 2 failed checks in a row |
| Errors | API and proxy logs shipped to central storage | Spike in 5xx responses |
| Certificates | Expiry check | Fewer than 21 days left |
| Disk and database size | Host metrics | Over 80% full |
| Security | Firewall and identity provider alerts | Any high-severity alert |

[CLIENT] Who receives alerts: ______

## Updates

| What | How often |
| --- | --- |
| Dependabot pull requests (Maven, npm, Docker images, GitHub Actions) | Review weekly; merge security fixes within 7 days |
| Server operating system | Monthly, or within 7 days for critical security fixes |
| WordPress core minor releases | Automatic |
| WordPress plugins and themes | Weekly review; security fixes within 7 days |
| Java and Node major versions | Plan before their end of support |

[CLIENT] Adjust these targets to the contract.

## Backup and restore

**Backups:** a daily database dump, encrypted and stored away from the server, kept for at least 35 days.

```bash
docker compose -f deploy/docker-compose.yml exec -T db \
  pg_dump -U portal -d portal --format=custom > portal-$(date +%F).dump
```

**Restore test (quarterly):** restore the latest backup into staging, check that recent requests appear, and record the result in the evidence log.

```bash
docker compose -f deploy/docker-compose.yml exec -T db \
  pg_restore -U portal -d portal --clean --if-exists < portal-YYYY-MM-DD.dump
```

[CLIENT] Backup storage location: ______ · Retention: ______ · Recovery time target: ______ · Maximum acceptable data loss: ______
