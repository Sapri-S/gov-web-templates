/**
 * [CLIENT] Per-client settings. Everything a client changes about wording and
 * identity lives here or in theme/tokens.css, not inside components.
 */
export const site = {
  name: 'City of Example',
  tagline: 'Resident services',
  /** Show the "An official website of the United States government" banner. Only for .gov sites. */
  showGovBanner: false,
  contactPhone: '301-555-0100',
  serviceRequestCategoryLabels: {
    pothole: 'Pothole',
    streetlight: 'Streetlight out',
    'trash-and-recycling': 'Missed trash or recycling',
    graffiti: 'Graffiti',
    other: 'Something else',
  } as Record<string, string>,
};
