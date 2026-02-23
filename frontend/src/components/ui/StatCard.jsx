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