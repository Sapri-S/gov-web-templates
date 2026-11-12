import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { ServiceRequestForm } from './ServiceRequestForm';
import { StatusLookup } from './StatusLookup';
import { accessibilityViolations } from '../../test/a11y';

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });
}

function mockApi(onSubmit: () => Response) {
  return vi.spyOn(globalThis, 'fetch').mockImplementation(async (input) => {
    const url = String(input);
    if (url.endsWith('/categories')) return jsonResponse(200, ['pothole', 'other']);
    return onSubmit();
  });
}

describe('ServiceRequestForm', () => {
  it('has no detectable accessibility violations', async () => {
    mockApi(() => jsonResponse(500, {}));
    const { container } = render(<ServiceRequestForm />);
    await screen.findByRole('option', { name: 'Pothole' });
    expect(await accessibilityViolations(container)).toEqual([]);
  });

  it('shows the tracking number after a successful submit', async () => {
    const fetchMock = mockApi(() =>
      jsonResponse(201, {
        trackingNumber: 'SR-7KQ4MZ2PXA',
        category: 'pothole',
        status: 'SUBMITTED',
        submittedAt: '2026-11-11T12:00:00Z',
        updatedAt: '2026-11-11T12:00:00Z',
      }),
    );
    const user = userEvent.setup();
    render(<ServiceRequestForm />);

    await user.selectOptions(await screen.findByLabelText('What type of problem is it?'), 'pothole');
    await user.type(screen.getByLabelText('Describe the problem'), 'Deep pothole');
    await user.click(screen.getByRole('button', { name: 'Submit request' }));

    expect(await screen.findByText('SR-7KQ4MZ2PXA')).toBeInTheDocument();
    const [, init] = fetchMock.mock.calls.at(-1)!;
    expect(JSON.parse(String(init?.body))).toMatchObject({ category: 'pothole', description: 'Deep pothole' });
  });

  it('shows API field errors next to fields and focuses the summary', async () => {
    mockApi(() =>
      jsonResponse(400, { detail: 'Some fields need attention.', errors: { description: 'Describe the problem.' } }),
    );
    const user = userEvent.setup();
    const { container } = render(<ServiceRequestForm />);
    await screen.findByRole('option', { name: 'Pothole' });

    await user.click(screen.getByRole('button', { name: 'Submit request' }));

    const summary = await screen.findByRole('alert');
    await waitFor(() => expect(summary).toHaveFocus());
    expect(screen.getByRole('link', { name: 'Describe the problem.' })).toHaveAttribute('href', '#sr-description');
    expect(screen.getByLabelText('Describe the problem')).toHaveAttribute('aria-invalid', 'true');
    expect(await accessibilityViolations(container)).toEqual([]);
  });
});

describe('StatusLookup', () => {
  it('shows a friendly message when the number is not found', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(404, { detail: 'Not found' }));
    const user = userEvent.setup();
    render(<StatusLookup />);

    await user.type(screen.getByLabelText('Tracking number'), 'SR-2222222222');
    await user.click(screen.getByRole('button', { name: 'Check status' }));

    expect(await screen.findByText(/couldn’t find a request/)).toBeInTheDocument();
  });

  it('shows the status in plain words', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(200, {
        trackingNumber: 'SR-7KQ4MZ2PXA',
        category: 'pothole',
        status: 'IN_PROGRESS',
        submittedAt: '2026-11-01T12:00:00Z',
        updatedAt: '2026-11-05T12:00:00Z',
      }),
    );
    const user = userEvent.setup();
    render(<StatusLookup />);

    await user.type(screen.getByLabelText('Tracking number'), 'SR-7KQ4MZ2PXA');
    await user.click(screen.getByRole('button', { name: 'Check status' }));

    expect(await screen.findByText('Status: Work in progress')).toBeInTheDocument();
  });
});
