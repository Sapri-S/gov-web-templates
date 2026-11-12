import { useEffect, useRef, useState } from 'react';
import { ApiError, type FieldErrors } from '../api/client';

/**
 * Shared error handling for forms: field messages come from the API, and the
 * error summary receives focus after each failed submit so screen reader and
 * keyboard users hear what went wrong.
 */
export function useFormErrors(fieldIdPrefix: string) {
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [generalError, setGeneralError] = useState<string>('');
  const [attempt, setAttempt] = useState(0);
  const summaryRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (attempt > 0 && Object.keys(fieldErrors).length > 0) {
      summaryRef.current?.focus();
    }
  }, [attempt, fieldErrors]);

  function handle(error: unknown) {
    if (error instanceof ApiError && Object.keys(error.fieldErrors).length > 0) {
      setFieldErrors(error.fieldErrors);
      setGeneralError('');
    } else {
      setFieldErrors({});
      setGeneralError(error instanceof Error ? error.message : 'Something went wrong. Try again later.');
    }
    setAttempt((n) => n + 1);
  }

  function clear() {
    setFieldErrors({});
    setGeneralError('');
  }

  const summary = Object.entries(fieldErrors).map(([field, message]) => ({
    id: `${fieldIdPrefix}${field}`,
    message,
  }));

  return { fieldErrors, generalError, summary, summaryRef, handle, clear };
}
