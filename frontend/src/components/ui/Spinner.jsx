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