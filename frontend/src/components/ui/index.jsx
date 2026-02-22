import React from 'react';

// ── Button ────────────────────────────────────────────────────────
export function Button({ children, variant = 'primary', size, disabled, loading, onClick, type = 'button', style }) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={`btn btn-${variant}${size ? ` btn-${size}` : ''}`}
      style={style}
    >
      {loading && <span style={{ fontSize: 12 }}>⏳</span>}
      {children}
    </button>
  );
}

// ── Spinner ───────────────────────────────────────────────────────
export function Spinner({ size = 20 }) {
  return (
    <div style={{
      width: size, height: size, border: `2px solid var(--gray-200)`,
      borderTop: `2px solid var(--primary)`, borderRadius: '50%',
      animation: 'spin 0.7s linear infinite',
      display: 'inline-block',
    }}>
      <style>{`@keyframes spin { to { transform: rotate(360deg) } }`}</style>
    </div>
  );
}

// ── StatusBadge ───────────────────────────────────────────────────
const STATUS_STYLES = {
  PENDING:   { background: 'var(--gray-100)', color: 'var(--gray-600)' },
  QUEUED:    { background: '#e0f2fe',          color: '#0369a1' },
  SENT:      { background: '#eff6ff',          color: 'var(--primary)' },
  DELIVERED: { background: '#f0fdf4',          color: 'var(--success)' },
  FAILED:    { background: '#fef2f2',          color: 'var(--danger)' },
  BOUNCED:   { background: '#fffbeb',          color: 'var(--warning)' },
};

export function StatusBadge({ status }) {
  const s = STATUS_STYLES[status] || STATUS_STYLES.PENDING;
  return <span className="badge" style={s}>{status}</span>;
}

// ── Card ──────────────────────────────────────────────────────────
export function Card({ children, style }) {
  return <div className="card" style={style}>{children}</div>;
}

// ── Field ─────────────────────────────────────────────────────────
export function Field({ label, error, hint, required, children }) {
  return (
    <div className="form-group">
      {label && <label>{label}{required && <span style={{ color: 'var(--danger)', marginLeft: 3 }}>*</span>}</label>}
      {children}
      {hint && !error && <span className="form-hint">{hint}</span>}
      {error && <span className="form-error">{error}</span>}
    </div>
  );
}

// ── Input ─────────────────────────────────────────────────────────
export function Input({ error, ...props }) {
  return <input className={`form-control${error ? ' error' : ''}`} {...props} />;
}

// ── Textarea ──────────────────────────────────────────────────────
export function Textarea({ error, rows = 5, ...props }) {
  return <textarea className={`form-control${error ? ' error' : ''}`} rows={rows} {...props} />;
}

// ── Select ────────────────────────────────────────────────────────
export function Select({ error, children, ...props }) {
  return (
    <select className={`form-control${error ? ' error' : ''}`} {...props}>
      {children}
    </select>
  );
}

// ── TagInput ──────────────────────────────────────────────────────
export function TagInput({ value = [], onChange, placeholder, error }) {
  const [input, setInput] = React.useState('');
  const ref = React.useRef();

  const add = (raw) => {
    const tags = raw.split(/[\s,;]+/).map((t) => t.trim()).filter(Boolean);
    onChange([...new Set([...value, ...tags])]);
    setInput('');
  };
  const remove = (i) => onChange(value.filter((_, idx) => idx !== i));

  const onKey = (e) => {
    if (['Enter', ',', 'Tab'].includes(e.key)) { e.preventDefault(); if (input.trim()) add(input); }
    else if (e.key === 'Backspace' && !input && value.length) remove(value.length - 1);
  };

  return (
    <div
      onClick={() => ref.current?.focus()}
      className={`form-control${error ? ' error' : ''}`}
      style={{ display: 'flex', flexWrap: 'wrap', gap: 4, minHeight: 38, cursor: 'text', padding: '4px 8px' }}
    >
      {value.map((tag, i) => (
        <span key={i} style={{ display: 'inline-flex', alignItems: 'center', gap: 4, background: '#eff6ff', color: 'var(--primary)', borderRadius: 4, padding: '1px 6px', fontSize: 12 }}>
          {tag}
          <button onClick={(e) => { e.stopPropagation(); remove(i); }} style={{ color: 'var(--gray-400)', lineHeight: 1, fontSize: 14 }}>×</button>
        </span>
      ))}
      <input
        ref={ref}
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyDown={onKey}
        onBlur={() => { if (input.trim()) add(input); }}
        onPaste={(e) => { e.preventDefault(); add(e.clipboardData.getData('text')); }}
        placeholder={value.length === 0 ? placeholder : ''}
        style={{ border: 'none', outline: 'none', background: 'transparent', fontSize: 14, flex: 1, minWidth: 100, padding: '2px 0' }}
      />
    </div>
  );
}

// ── Modal ─────────────────────────────────────────────────────────
export function Modal({ open, onClose, title, children, width = 520 }) {
  React.useEffect(() => {
    const h = (e) => { if (e.key === 'Escape') onClose(); };
    if (open) document.addEventListener('keydown', h);
    return () => document.removeEventListener('keydown', h);
  }, [open, onClose]);

  if (!open) return null;
  return (
    <div onClick={onClose} style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: 16 }}>
      <div onClick={(e) => e.stopPropagation()} style={{ background: '#fff', borderRadius: 8, width: '100%', maxWidth: width, maxHeight: '90vh', overflow: 'auto', boxShadow: '0 10px 40px rgba(0,0,0,0.15)' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '16px 20px', borderBottom: '1px solid var(--gray-200)' }}>
          <h3 style={{ fontSize: 16, fontWeight: 600 }}>{title}</h3>
          <button onClick={onClose} className="btn btn-ghost btn-sm">✕</button>
        </div>
        <div style={{ padding: 20 }}>{children}</div>
      </div>
    </div>
  );
}

// ── EmptyState ────────────────────────────────────────────────────
export function EmptyState({ title, description, action }) {
  return (
    <div style={{ textAlign: 'center', padding: '48px 20px', color: 'var(--gray-400)' }}>
      <div style={{ fontSize: 32, marginBottom: 8 }}>📭</div>
      <div style={{ fontWeight: 500, color: 'var(--gray-600)', marginBottom: 6 }}>{title}</div>
      {description && <p style={{ fontSize: 13, marginBottom: 12 }}>{description}</p>}
      {action}
    </div>
  );
}

// ── PageHeader ────────────────────────────────────────────────────
export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="page-header">
      <div>
        <div className="page-title">{title}</div>
        {subtitle && <div className="page-subtitle">{subtitle}</div>}
      </div>
      {actions && <div style={{ display: 'flex', gap: 8 }}>{actions}</div>}
    </div>
  );
}

// ── StatCard ──────────────────────────────────────────────────────
export function StatCard({ label, value, color, onClick }) {
  return (
    <div
      onClick={onClick}
      className="card"
      style={{ cursor: onClick ? 'pointer' : 'default', padding: '16px' }}
    >
      <div style={{ fontSize: 12, color: 'var(--gray-500)', marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.04em' }}>{label}</div>
      <div style={{ fontSize: 28, fontWeight: 700, color: color || 'var(--gray-900)' }}>{value ?? 0}</div>
    </div>
  );
}

// ── Pagination ────────────────────────────────────────────────────
export function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;
  return (
    <div style={{ display: 'flex', gap: 4, justifyContent: 'center', marginTop: 16 }}>
      <button className="btn btn-secondary btn-sm" disabled={page === 0} onClick={() => onPageChange(page - 1)}>Prev</button>
      {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => (
        <button key={i} className={`btn btn-sm ${i === page ? 'btn-primary' : 'btn-secondary'}`} onClick={() => onPageChange(i)}>{i + 1}</button>
      ))}
      <button className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>Next</button>
    </div>
  );
}

// ── Table ─────────────────────────────────────────────────────────
export function Table({ columns, data, onRowClick, emptyState }) {
  if (!data?.length) return emptyState || <EmptyState title="No records found" />;
  return (
    <div style={{ overflowX: 'auto' }}>
      <table>
        <thead>
          <tr>{columns.map((c) => <th key={c.key}>{c.label}</th>)}</tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={row.id || i} onClick={() => onRowClick?.(row)} style={{ cursor: onRowClick ? 'pointer' : 'default' }}>
              {columns.map((c) => (
                <td key={c.key} style={{ maxWidth: c.maxWidth }}>
                  {c.render ? c.render(row[c.key], row) : row[c.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}