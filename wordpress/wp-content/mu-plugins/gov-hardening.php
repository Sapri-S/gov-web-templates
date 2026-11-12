<?php
/**
 * Plugin Name: Gov Hardening
 * Description: Baseline security settings for government WordPress sites. Must-use plugin, so it can't be switched off in the admin.
 * Version: 0.1.0
 *
 * Pair with the constants in wp-config-hardening.php. Settings that need the web
 * server (TLS, rate limits, blocking /wp-admin by IP) belong in hosting or the WAF.
 */

if (!defined('ABSPATH')) {
    exit;
}

// XML-RPC is a common brute-force and amplification target, and modern sites don't need it.
add_filter('xmlrpc_enabled', '__return_false');
add_filter('wp_headers', static function (array $headers): array {
    unset($headers['X-Pingback']);
    return $headers;
});

// Don't advertise the WordPress version.
remove_action('wp_head', 'wp_generator');
add_filter('the_generator', '__return_empty_string');

// Stop anonymous visitors from listing user accounts (login names) through the REST API or ?author=N.
add_filter('rest_endpoints', static function (array $endpoints): array {
    if (!is_user_logged_in()) {
        unset($endpoints['/wp/v2/users'], $endpoints['/wp/v2/users/(?P<id>[\d]+)']);
    }
    return $endpoints;
});
add_action('template_redirect', static function (): void {
    if (!is_user_logged_in() && isset($_GET['author'])) {
        wp_safe_redirect(home_url('/'), 301);
        exit;
    }
});

// Give the same message for a wrong username and a wrong password.
add_filter('login_errors', static function (): string {
    return __('The username or password is incorrect.', 'gov-hardening');
});

// Security headers. Set the Content-Security-Policy at the web server once the site's
// third-party services (analytics, translation, maps) are known.
add_action('send_headers', static function (): void {
    header('X-Content-Type-Options: nosniff');
    header('X-Frame-Options: SAMEORIGIN');
    header('Referrer-Policy: strict-origin-when-cross-origin');
    header('Permissions-Policy: camera=(), microphone=(), geolocation=()');
    if (is_ssl()) {
        header('Strict-Transport-Security: max-age=31536000; includeSubDomains');
    }
});

// Keep application passwords off unless a client project needs them for an integration.
add_filter('wp_is_application_passwords_available', '__return_false');
