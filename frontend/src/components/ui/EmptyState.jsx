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