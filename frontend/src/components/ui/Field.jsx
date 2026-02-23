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
