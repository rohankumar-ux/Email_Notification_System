export function Input({ error, ...props }) {
  return <input className={`form-control${error ? ' error' : ''}`} {...props} />;
}
