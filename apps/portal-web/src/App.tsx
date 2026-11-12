import { site } from './config/site';
import { DynamicForm } from './features/forms/DynamicForm';
import { ServiceRequestForm } from './features/service-requests/ServiceRequestForm';
import { StatusLookup } from './features/service-requests/StatusLookup';
import { PageSection } from './ui';

export function App() {
  return (
    <>
      <a className="usa-skipnav" href="#main-content">
        Skip to main content
      </a>
      {site.showGovBanner && (
        <section className="usa-banner" aria-label="Official website of the United States government">
          <div className="usa-banner__header">
            <div className="usa-banner__inner">
              <p className="usa-banner__header-text">An official website of the United States government</p>
            </div>
          </div>
        </section>
      )}
      <header className="usa-header usa-header--basic site-header">
        <div className="usa-nav-container">
          <div className="usa-navbar">
            <div className="usa-logo">
              <em className="usa-logo__text">
                <a href="/" title={`${site.name} home`}>
                  {site.name}
                </a>
              </em>
            </div>
          </div>
        </div>
      </header>

      <main id="main-content" className="grid-container padding-y-4" tabIndex={-1}>
        <h1>{site.tagline}</h1>
        <PageSection id="report" title="Report a problem">
          <ServiceRequestForm />
        </PageSection>
        <PageSection id="status" title="Check a request’s status">
          <StatusLookup />
        </PageSection>
        <PageSection id="contact" title="Contact us">
          <DynamicForm formKey="contact" />
        </PageSection>
      </main>

      <footer className="usa-footer usa-footer--slim">
        <div className="usa-footer__secondary-section">
          <div className="grid-container">
            <p>
              {site.name} · Call <a href={`tel:${site.contactPhone}`}>{site.contactPhone}</a> ·{' '}
              <a href="/accessibility">Accessibility</a> · <a href="/privacy">Privacy</a>
            </p>
          </div>
        </div>
      </footer>
    </>
  );
}
