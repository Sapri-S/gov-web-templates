import axe from 'axe-core';

/** Runs axe against a rendered container and returns readable violation summaries. */
export async function accessibilityViolations(container: HTMLElement): Promise<string[]> {
  const results = await axe.run(container, {
    runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'] },
    // jsdom has no layout engine, so color contrast can't be measured here. Check it in a real browser.
    rules: { 'color-contrast': { enabled: false } },
  });
  return results.violations.map((v) => `${v.id}: ${v.help} (${v.nodes.length} element(s))`);
}
