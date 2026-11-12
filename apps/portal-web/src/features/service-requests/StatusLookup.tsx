import { useState, type FormEvent } from 'react';
import { api, ApiError, type PublicStatus, type ServiceRequestStatus } from '../../api/client';
import { site } from '../../config/site';
import { Alert, Button, TextInput } from '../../ui';

const statusLabels: Record<ServiceRequestStatus, string> = {
  SUBMITTED: 'Received',
  IN_REVIEW: 'Being reviewed',
  IN_PROGRESS: 'Work in progress',
  RESOLVED: 'Fixed',
  CLOSED: 'Closed',
};

const dateFormat = new Intl.DateTimeFormat('en-US', { dateStyle: 'long' });

export function StatusLookup() {
  const [trackingNumber, setTrackingNumber] = useState('');
  const [result, setResult] = useState<PublicStatus | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setResult(null);
    setError('');
    if (!trackingNumber.trim()) {
      setError('Enter your tracking number.');
      return;
    }
    setLoading(true);
    try {
      setResult(await api.serviceRequestStatus(trackingNumber));
    } catch (e) {
      setError(
        e instanceof ApiError && e.status === 404
          ? 'We couldn’t find a request with that tracking number. Check it and try again.'
          : e instanceof Error
            ? e.message
            : 'Something went wrong.',
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="usa-form" onSubmit={onSubmit} noValidate>
      <TextInput
        id="tracking-number"
        label="Tracking number"
        hint="It starts with SR-, for example SR-7KQ4MZ2PXA"
        required
        value={trackingNumber}
        onChange={setTrackingNumber}
        error={error}
      />
      <Button type="submit" disabled={loading}>
        {loading ? 'Checking…' : 'Check status'}
      </Button>
      {result && (
        <Alert type="info" heading={`Status: ${statusLabels[result.status]}`}>
          <p>
            {site.serviceRequestCategoryLabels[result.category] ?? result.category}, submitted{' '}
            {dateFormat.format(new Date(result.submittedAt))}. Last updated{' '}
            {dateFormat.format(new Date(result.updatedAt))}.
          </p>
        </Alert>
      )}
    </form>
  );
}
