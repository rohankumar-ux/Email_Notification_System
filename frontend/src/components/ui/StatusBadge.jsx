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