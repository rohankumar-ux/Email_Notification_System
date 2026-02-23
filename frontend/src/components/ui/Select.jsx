export function Select({ error, children, ...props }) {
  return (
    <select className={`form-control${error ? ' error' : ''}`} {...props}>
      {children}
    </select>
  );
}