
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