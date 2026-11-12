# Accessibility

Every site and application built from these templates must meet WCAG 2.1 Level AA. Automated checks catch only part of the problems, so every release also gets manual keyboard and screen reader testing.

## Standards

| Standard | Applies to | Notes |
| --- | --- | --- |
| WCAG 2.1 Level AA | All projects | The technical standard in the DOJ's ADA Title II rule for state and local governments. Compliance dates: April 26, 2027 for governments serving 50,000 or more people; April 26, 2028 for smaller governments and special districts. |
| Section 508 | Federal projects | Uses WCAG 2.0 AA; meeting WCAG 2.1 AA covers it. |
| Maryland non-visual access (NVA) | Maryland state agency projects | [CLIENT] Confirm current requirements with the agency. |

[CLIENT] Record which standards and which compliance date apply to this client.

## What the template already does

- Every field has a visible label; optional fields say "(optional)".
- Hints and error messages are tied to their fields with `aria-describedby`, and invalid fields have `aria-invalid`.
- After a failed submit, an error summary lists each problem with a link to the field and receives focus.
- Success and status messages use `role="status"`; errors use `role="alert"`.
- A skip link, one `h1` per page, and sections labeled by their headings.
- The spam trap field is hidden from screen readers and keyboard focus.
- Automated axe checks for WCAG 2.0 and 2.1 A and AA run in the frontend tests on every push.
- WordPress theme: visible focus outlines, underlined body links, fixed type scale and USWDS colors.

## Testing process

Run for every release that changes anything people see.

1. **Automated:** CI runs axe on each component. Then run axe or WAVE in a browser on every page type, because the CI checks can't measure color contrast.
2. **Keyboard only:** Complete every task without a mouse. Check focus is always visible, the order makes sense and nothing traps focus.
3. **Screen reader:** Complete every task with NVDA and Firefox or Chrome on Windows, and VoiceOver and Safari on Mac or iPhone.
4. **Zoom and reflow:** At 200% zoom and at 320 pixels wide, nothing is cut off and there's no sideways scrolling.
5. **Color and contrast:** Text at least 4.5:1 (3:1 for large text and interface parts). Nothing relies on color alone.
6. **Forms:** Submit each form empty and with wrong values; every error is announced and explains how to fix it.
7. **Documents:** Every PDF or Word file linked from the site is accessible, or has an accessible HTML version.

## Results

Record results for each release. Keep the filled-in copy in the client's project folder.

| Date | Release | Page or task | Check | Result | Issue link | Fixed in |
| --- | --- | --- | --- | --- | --- | --- |
| | | | Automated (axe) | | | |
| | | | Keyboard | | | |
| | | | NVDA | | | |
| | | | VoiceOver | | | |
| | | | Zoom and reflow | | | |
| | | | Contrast | | | |
| | | | Documents | | | |

Publish an accessibility statement on every site with a way to report problems, and answer reports within the time the client sets.
