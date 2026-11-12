# WordPress starter

For clients who need a content site that staff can edit, which is what most local RFPs ask for.

## What's here

| Path | What it does |
| --- | --- |
| `wp-content/themes/gov-starter/` | Block theme with USWDS colors, Public Sans, a type scale and visible focus styles |
| `wp-content/mu-plugins/gov-hardening.php` | Security baseline that can't be switched off in the admin |
| `wp-config-hardening.php` | Lines to paste into `wp-config.php` |
| `docker-compose.yml` | Local development only |

## Run it locally

```bash
cd wordpress
echo "WP_DB_PASSWORD=$(openssl rand -hex 16)" > .env
docker compose up -d
```

Open http://localhost:8081, finish the install, then go to **Appearance > Themes** and activate **Gov Starter**.

## Start a client site

1. Copy this folder into the client's repository.
2. In `theme.json`, change the palette to the client's colors. Keep text contrast at 4.5:1 or higher.
3. Load Public Sans from the client's own server (download it from the USWDS package) rather than a third-party font service.
4. Replace the `[CLIENT]` placeholders in `parts/footer.html`.
5. Add the plugins the client approved in `docs/client-intake.md`, and record each one in the site's plugin list with its owner and update plan.

## Plugins

Keep plugins to a minimum: each one is code you're responsible for patching. Before adding one, check that it's actively maintained, has recent updates, and is needed. Forms and service requests can come from the Spring Boot portal API instead of a forms plugin, which keeps resident data out of WordPress.
