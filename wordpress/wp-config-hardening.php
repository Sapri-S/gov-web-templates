<?php
/**
 * Paste these lines into wp-config.php ABOVE the line that says
 * "That's all, stop editing!". Secrets come from environment variables,
 * never from files in the repository.
 */

// Block the theme and plugin file editors in the admin.
define('DISALLOW_FILE_EDIT', true);

// Optional: block ALL plugin, theme and core installs and updates from the admin,
// so changes only arrive through deployments. This also turns off automatic
// updates, including the security releases below, so enable it only when the
// client's maintenance plan applies updates through the deployment pipeline.
// define('DISALLOW_FILE_MODS', true);

// Admin and login only over HTTPS.
define('FORCE_SSL_ADMIN', true);

// Apply minor (security) core releases automatically.
define('WP_AUTO_UPDATE_CORE', 'minor');

// Never show PHP errors to visitors.
define('WP_DEBUG', false);
define('WP_DEBUG_DISPLAY', false);
@ini_set('display_errors', '0');

// Keep a limited number of revisions so the database doesn't grow without bound.
define('WP_POST_REVISIONS', 20);
