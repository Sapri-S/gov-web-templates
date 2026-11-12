/**
 * Design-system adapter. Feature code imports ONLY from this file.
 * This version renders USWDS markup. To switch a client to MDWDS or their own
 * design system, rewrite this file; feature code and the API stay the same.
 */
import { forwardRef, type ReactNode } from 'react';
import './ui.css';

interface FieldProps {
  id: string;
  label: string;
  hint?: string;
  error?: string;
  required?: boolean;
}

function describedBy(id: string, hint?: string, error?: string): string | undefined {
  const ids = [hint ? `${id}-hint` : '', error ? `${id}-error` : ''].filter(Boolean);
  return ids.length ? ids.join(' ') : undefined;
}

function FieldShell({ id, label, hint, error, required, children }: FieldProps & { children: ReactNode }) {
  return (
    <div className={`usa-form-group${error ? ' usa-form-group--error' : ''}`}>
      <label className={`usa-label${error ? ' usa-label--error' : ''}`} htmlFor={id}>
        {label}
        {!required && <span className="usa-hint"> (optional)</span>}
      </label>
      {hint && (
        <div className="usa-hint" id={`${id}-hint`}>
          {hint}
        </div>
      )}
      {error && (
        <span className="usa-error-message" id={`${id}-error`}>
          {error}
        </span>
      )}
      {children}
    </div>
  );
}

export function TextInput(
  props: FieldProps & {
    value: string;
    onChange: (value: string) => void;
    type?: 'text' | 'email' | 'tel';
    maxLength?: number;
    autoComplete?: string;
  },
) {
  const { id, hint, error, required, value, onChange, type = 'text', maxLength, autoComplete } = props;
  return (
    <FieldShell {...props}>
      <input
        className={`usa-input${error ? ' usa-input--error' : ''}`}
        id={id}
        name={id}
        type={type}
        value={value}
        required={required}
        maxLength={maxLength}
        autoComplete={autoComplete}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy(id, hint, error)}
        onChange={(e) => onChange(e.target.value)}
      />
    </FieldShell>
  );
}

export function TextArea(
  props: FieldProps & { value: string; onChange: (value: string) => void; maxLength?: number },
) {
  const { id, hint, error, required, value, onChange, maxLength } = props;
  return (
    <FieldShell {...props}>
      <textarea
        className={`usa-textarea${error ? ' usa-input--error' : ''}`}
        id={id}
        name={id}
        value={value}
        required={required}
        maxLength={maxLength}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy(id, hint, error)}
        onChange={(e) => onChange(e.target.value)}
      />
    </FieldShell>
  );
}

export function Select(
  props: FieldProps & {
    value: string;
    onChange: (value: string) => void;
    options: { value: string; label: string }[];
  },
) {
  const { id, hint, error, required, value, onChange, options } = props;
  return (
    <FieldShell {...props}>
      <select
        className={`usa-select${error ? ' usa-input--error' : ''}`}
        id={id}
        name={id}
        value={value}
        required={required}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy(id, hint, error)}
        onChange={(e) => onChange(e.target.value)}
      >
        <option value="">- Select -</option>
        {options.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
    </FieldShell>
  );
}

export function Checkbox({
  id,
  label,
  hint,
  error,
  checked,
  onChange,
}: FieldProps & { checked: boolean; onChange: (checked: boolean) => void }) {
  return (
    <div className={`usa-form-group${error ? ' usa-form-group--error' : ''}`}>
      {error && (
        <span className="usa-error-message" id={`${id}-error`}>
          {error}
        </span>
      )}
      <div className="usa-checkbox">
        <input
          className="usa-checkbox__input"
          id={id}
          name={id}
          type="checkbox"
          checked={checked}
          aria-invalid={error ? true : undefined}
          aria-describedby={describedBy(id, hint, error)}
          onChange={(e) => onChange(e.target.checked)}
        />
        <label className="usa-checkbox__label" htmlFor={id}>
          {label}
          {hint && (
            <span className="usa-checkbox__label-description" id={`${id}-hint`}>
              {hint}
            </span>
          )}
        </label>
      </div>
    </div>
  );
}

export function Button({
  children,
  type = 'button',
  disabled,
  onClick,
}: {
  children: ReactNode;
  type?: 'button' | 'submit';
  disabled?: boolean;
  onClick?: () => void;
}) {
  return (
    <button className="usa-button" type={type} disabled={disabled} onClick={onClick}>
      {children}
    </button>
  );
}

export function Alert({
  type,
  heading,
  children,
}: {
  type: 'success' | 'error' | 'info' | 'warning';
  heading: string;
  children?: ReactNode;
}) {
  return (
    <div className={`usa-alert usa-alert--${type}`} role={type === 'error' ? 'alert' : 'status'}>
      <div className="usa-alert__body">
        <h3 className="usa-alert__heading">{heading}</h3>
        {children && <div className="usa-alert__text">{children}</div>}
      </div>
    </div>
  );
}

/** Lists every field error with links to the fields. Receives focus after a failed submit. */
export const ErrorSummary = forwardRef<HTMLDivElement, { errors: { id: string; message: string }[] }>(
  function ErrorSummary({ errors }, ref) {
    if (errors.length === 0) return null;
    return (
      <div className="usa-alert usa-alert--error" role="alert" tabIndex={-1} ref={ref}>
        <div className="usa-alert__body">
          <h3 className="usa-alert__heading">
            {errors.length === 1 ? 'There’s 1 problem to fix' : `There are ${errors.length} problems to fix`}
          </h3>
          <ul className="usa-list">
            {errors.map((e) => (
              <li key={e.id}>
                <a href={`#${e.id}`}>{e.message}</a>
              </li>
            ))}
          </ul>
        </div>
      </div>
    );
  },
);

/** Hidden spam trap. Kept off-screen and out of the accessibility tree; people never fill it. */
export function HoneypotField({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  return (
    <div aria-hidden="true" className="portal-honeypot">
      <label htmlFor="website">Leave this field empty</label>
      <input
        id="website"
        name="website"
        type="text"
        tabIndex={-1}
        autoComplete="off"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}

export function PageSection({ id, title, children }: { id: string; title: string; children: ReactNode }) {
  return (
    <section className="margin-bottom-6" aria-labelledby={`${id}-title`}>
      <h2 id={`${id}-title`}>{title}</h2>
      {children}
    </section>
  );
}
