/**
 * Thin client for the portal API. All business rules (validation, statuses,
 * categories) live in the API; this file only moves data.
 */

export type FieldErrors = Record<string, string>;

export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: FieldErrors;

  constructor(status: number, message: string, fieldErrors: FieldErrors = {}) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(path, {
      ...init,
      headers: { Accept: 'application/json', ...(init?.body ? { 'Content-Type': 'application/json' } : {}) },
    });
  } catch {
    throw new ApiError(0, 'We couldn’t reach the server. Check your connection and try again.');
  }

  if (response.ok) {
    return (await response.json()) as T;
  }

  let detail = 'Something went wrong. Try again later.';
  let fieldErrors: FieldErrors = {};
  try {
    const problem = await response.json();
    if (typeof problem?.detail === 'string') detail = problem.detail;
    if (problem?.errors && typeof problem.errors === 'object') fieldErrors = problem.errors;
  } catch {
    // Response had no JSON body; keep the generic message.
  }
  throw new ApiError(response.status, detail, fieldErrors);
}

export type ServiceRequestStatus = 'SUBMITTED' | 'IN_REVIEW' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';

export interface PublicStatus {
  trackingNumber: string;
  category: string;
  status: ServiceRequestStatus;
  submittedAt: string;
  updatedAt: string;
}

export interface NewServiceRequest {
  category: string;
  description: string;
  location?: string;
  contactName?: string;
  contactEmail?: string;
  contactPhone?: string;
  website?: string;
}

export type FieldType = 'TEXT' | 'TEXTAREA' | 'EMAIL' | 'PHONE' | 'SELECT' | 'CHECKBOX';

export interface FieldDefinition {
  name: string;
  label: string;
  hint: string;
  type: FieldType;
  required: boolean;
  maxLength: number;
  options: string[];
}

export interface PublicForm {
  key: string;
  title: string;
  description: string;
  fields: FieldDefinition[];
}

export interface Receipt {
  referenceNumber: string;
  submittedAt: string;
}

export const api = {
  categories: () => request<string[]>('/api/service-requests/categories'),
  submitServiceRequest: (body: NewServiceRequest) =>
    request<PublicStatus>('/api/service-requests', { method: 'POST', body: JSON.stringify(body) }),
  serviceRequestStatus: (trackingNumber: string) =>
    request<PublicStatus>(`/api/service-requests/${encodeURIComponent(trackingNumber.trim())}`),
  form: (key: string) => request<PublicForm>(`/api/forms/${encodeURIComponent(key)}`),
  submitForm: (key: string, values: Record<string, string | boolean>, website = '') =>
    request<Receipt>(`/api/forms/${encodeURIComponent(key)}/submissions`, {
      method: 'POST',
      body: JSON.stringify({ values, website }),
    }),
};
