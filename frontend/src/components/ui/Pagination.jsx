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