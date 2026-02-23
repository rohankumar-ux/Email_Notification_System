import { EmptyState } from "./EmptyState";

export function Table({ columns, data, onRowClick, emptyState }) {
  if (!data?.length) return emptyState || <EmptyState title="No records found" />;
  return (
    <div style={{ overflowX: 'auto' }}>
      <table>
        <thead>
          <tr>{columns.map((c) => <th key={c.key}>{c.label}</th>)}</tr>
        </thead>
        <tbody>
          {data.map((row, i) => (
            <tr key={row.id || i} onClick={() => onRowClick?.(row)} style={{ cursor: onRowClick ? 'pointer' : 'default' }}>
              {columns.map((c) => (
                <td key={c.key} style={{ maxWidth: c.maxWidth }}>
                  {c.render ? c.render(row[c.key], row) : row[c.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}