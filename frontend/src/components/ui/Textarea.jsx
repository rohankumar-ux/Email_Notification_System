export function Textarea({ error, rows = 5, ...props }) {
  return <textarea className={`form-control${error ? ' error' : ''}`} rows={rows} {...props} />;
}