import { useEffect, useState, type FormEvent } from 'react';
import { api, type FieldDefinition, type PublicForm, type Receipt } from '../../api/client';
import { Alert, Button, Checkbox, ErrorSummary, HoneypotField, Select, TextArea, TextInput } from '../../ui';
import { useFormErrors } from '../useFormErrors';

type Values = Record<string, string | boolean>;

/** Renders any form the API defines. New client forms need YAML, not new React code. */
export function DynamicForm({ formKey }: { formKey: string }) {
  const [form, setForm] = useState<PublicForm | null>(null);
  const [values, setValues] = useState<Values>({});
  const [honeypot, setHoneypot] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [receipt, setReceipt] = useState<Receipt | null>(null);
  const prefix = `${formKey}-`;
  const errors = useFormErrors(prefix);

  useEffect(() => {
    api.form(formKey).then(setForm).catch(errors.handle);
  }, [formKey]);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    errors.clear();
    try {
      setReceipt(await api.submitForm(formKey, values, honeypot));
      setValues({});
    } catch (error) {
      errors.handle(error);
    } finally {
      setSubmitting(false);
    }
  }

  if (receipt) {
    return (
      <Alert type="success" heading="Thank you. We received your message.">
        <p>
          Your reference number is <strong>{receipt.referenceNumber}</strong>.
        </p>
      </Alert>
    );
  }
  if (!form) {
    return errors.generalError ? <Alert type="error" heading="This form isn’t available">{errors.generalError}</Alert> : null;
  }

  const field = (f: FieldDefinition) => {
    const common = {
      id: `${prefix}${f.name}`,
      label: f.label,
      hint: f.hint || undefined,
      required: f.required,
      error: errors.fieldErrors[f.name],
    };
    const text = typeof values[f.name] === 'string' ? (values[f.name] as string) : '';
    const setText = (v: string) => setValues((all) => ({ ...all, [f.name]: v }));
    switch (f.type) {
      case 'TEXTAREA':
        return <TextArea key={f.name} {...common} maxLength={f.maxLength} value={text} onChange={setText} />;
      case 'SELECT':
        return (
          <Select
            key={f.name}
            {...common}
            value={text}
            onChange={setText}
            options={f.options.map((o) => ({ value: o, label: o }))}
          />
        );
      case 'CHECKBOX':
        return (
          <Checkbox
            key={f.name}
            {...common}
            checked={values[f.name] === true}
            onChange={(c) => setValues((all) => ({ ...all, [f.name]: c }))}
          />
        );
      case 'EMAIL':
        return <TextInput key={f.name} {...common} type="email" autoComplete="email" maxLength={f.maxLength} value={text} onChange={setText} />;
      case 'PHONE':
        return <TextInput key={f.name} {...common} type="tel" autoComplete="tel" maxLength={f.maxLength} value={text} onChange={setText} />;
      default:
        return <TextInput key={f.name} {...common} maxLength={f.maxLength} value={text} onChange={setText} />;
    }
  };

  return (
    <form className="usa-form usa-form--large" onSubmit={onSubmit} noValidate aria-describedby={`${prefix}description`}>
      {form.description && <p id={`${prefix}description`}>{form.description}</p>}
      <ErrorSummary ref={errors.summaryRef} errors={errors.summary} />
      {errors.generalError && <Alert type="error" heading="Your message wasn’t sent">{errors.generalError}</Alert>}
      {form.fields.map(field)}
      <HoneypotField value={honeypot} onChange={setHoneypot} />
      <Button type="submit" disabled={submitting}>
        {submitting ? 'Sending…' : 'Send'}
      </Button>
    </form>
  );
}
