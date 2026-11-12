import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { DynamicForm } from './DynamicForm';
import { accessibilityViolations } from '../../test/a11y';

const contactForm = {
  key: 'contact',
  title: 'Contact us',
  description: 'Send a question or comment.',
  fields: [
    { name: 'name', label: 'Your name', hint: '', type: 'TEXT', required: true, maxLength: 100, options: [] },
    { name: 'email', label: 'Email address', hint: '', type: 'EMAIL', required: true, maxLength: 254, options: [] },
    {
      name: 'topic',
      label: 'Topic',
      hint: '',
      type: 'SELECT',
      required: true,
      maxLength: 100,
      options: ['General question', 'Public records'],
    },
    { name: 'message', label: 'Message', hint: '', type: 'TEXTAREA', required: true, maxLength: 2000, options: [] },
    { name: 'consent', label: 'You may contact me', hint: '', type: 'CHECKBOX', required: false, maxLength: 0, options: [] },
  ],
};

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });
}

describe('DynamicForm', () => {
  it('renders every field type from the API definition accessibly', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, contactForm));
    const { container } = render(<DynamicForm formKey="contact" />);

    expect(await screen.findByLabelText('Your name')).toBeInTheDocument();
    expect(screen.getByLabelText('Email address')).toHaveAttribute('type', 'email');
    expect(screen.getByLabelText('Topic').tagName).toBe('SELECT');
    expect(screen.getByLabelText('Message').tagName).toBe('TEXTAREA');
    expect(screen.getByLabelText('You may contact me')).toHaveAttribute('type', 'checkbox');
    expect(await accessibilityViolations(container)).toEqual([]);
  });

  it('submits values and shows the reference number', async () => {
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(jsonResponse(200, contactForm))
      .mockResolvedValueOnce(jsonResponse(201, { referenceNumber: 'FM-ABCDEFGHJK', submittedAt: '2026-11-11T12:00:00Z' }));
    const user = userEvent.setup();
    render(<DynamicForm formKey="contact" />);

    await user.type(await screen.findByLabelText('Your name'), 'Pat');
    await user.type(screen.getByLabelText('Email address'), 'pat@example.com');
    await user.selectOptions(screen.getByLabelText('Topic'), 'Public records');
    await user.type(screen.getByLabelText('Message'), 'Hello');
    await user.click(screen.getByLabelText('You may contact me'));
    await user.click(screen.getByRole('button', { name: 'Send' }));

    expect(await screen.findByText('FM-ABCDEFGHJK')).toBeInTheDocument();
    const [url, init] = fetchMock.mock.calls[1];
    expect(String(url)).toBe('/api/forms/contact/submissions');
    expect(JSON.parse(String(init?.body))).toEqual({
      values: { name: 'Pat', email: 'pat@example.com', topic: 'Public records', message: 'Hello', consent: true },
      website: '',
    });
  });
});
