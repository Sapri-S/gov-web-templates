import { useEffect, useState, type FormEvent } from 'react';
import { api, type NewServiceRequest, type PublicStatus } from '../../api/client';
import { site } from '../../config/site';
import { Alert, Button, ErrorSummary, HoneypotField, Select, TextArea, TextInput } from '../../ui';
import { useFormErrors } from '../useFormErrors';

const empty: NewServiceRequest = {
  category: '',
  description: '',
  location: '',
  contactName: '',
  contactEmail: '',
  contactPhone: '',
  website: '',
};

const PREFIX = 'sr-';

export function ServiceRequestForm() {
  const [categories, setCategories] = useState<string[]>([]);
  const [values, setValues] = useState<NewServiceRequest>(empty);
  const [submitting, setSubmitting] = useState(false);
  const [created, setCreated] = useState<PublicStatus | null>(null);
  const errors = useFormErrors(PREFIX);

  useEffect(() => {
    api.categories().then(setCategories).catch(errors.handle);
    // Load once on mount.
  }, []);

  const set = (field: keyof NewServiceRequest) => (value: string) => setValues((v) => ({ ...v, [field]: value }));

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    errors.clear();
    try {
      setCreated(await api.submitServiceRequest(values));
      setValues(empty);
    } catch (error) {
      errors.handle(error);
    } finally {
      setSubmitting(false);
    }
  }

  if (created) {
    return (
      <Alert type="success" heading="We received your request">
        <p>
          Your tracking number is <strong>{created.trackingNumber}</strong>. Save it to check the status later.
        </p>
        <p>
          <button className="usa-button usa-button--unstyled" type="button" onClick={() => setCreated(null)}>
            Report another problem
          </button>
        </p>
      </Alert>
    );
  }

  const e = errors.fieldErrors;
  return (
    <form className="usa-form usa-form--large" onSubmit={onSubmit} noValidate>
      <ErrorSummary ref={errors.summaryRef} errors={errors.summary} />
      {errors.generalError && <Alert type="error" heading="Your request wasn’t sent">{errors.generalError}</Alert>}

      <Select
        id={`${PREFIX}category`}
        label="What type of problem is it?"
        required
        value={values.category}
        onChange={set('category')}
        error={e.category}
        options={categories.map((c) => ({ value: c, label: site.serviceRequestCategoryLabels[c] ?? c }))}
      />
      <TextArea
        id={`${PREFIX}description`}
        label="Describe the problem"
        hint="Include anything that helps crews find and fix it."
        required
        maxLength={4000}
        value={values.description}
        onChange={set('description')}
        error={e.description}
      />
      <TextInput
        id={`${PREFIX}location`}
        label="Where is it?"
        hint="Street address or nearest intersection"
        maxLength={300}
        value={values.location ?? ''}
        onChange={set('location')}
        error={e.location}
      />

      <fieldset className="usa-fieldset">
        <legend className="usa-legend">How can we reach you?</legend>
        <p className="usa-hint">We only use this to ask questions about your request.</p>
        <TextInput
          id={`${PREFIX}contactName`}
          label="Name"
          autoComplete="name"
          maxLength={100}
          value={values.contactName ?? ''}
          onChange={set('contactName')}
          error={e.contactName}
        />
        <TextInput
          id={`${PREFIX}contactEmail`}
          label="Email address"
          type="email"
          autoComplete="email"
          maxLength={254}
          value={values.contactEmail ?? ''}
          onChange={set('contactEmail')}
          error={e.contactEmail}
        />
        <TextInput
          id={`${PREFIX}contactPhone`}
          label="Phone number"
          type="tel"
          autoComplete="tel"
          maxLength={30}
          value={values.contactPhone ?? ''}
          onChange={set('contactPhone')}
          error={e.contactPhone}
        />
      </fieldset>

      <HoneypotField value={values.website ?? ''} onChange={set('website')} />
      <Button type="submit" disabled={submitting}>
        {submitting ? 'Sending…' : 'Submit request'}
      </Button>
    </form>
  );
}
