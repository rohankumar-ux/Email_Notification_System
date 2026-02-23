import { useState, useEffect, useCallback } from 'react';
import toast from 'react-hot-toast';
import { useSearchParams } from 'react-router-dom';
import { emailApi } from '../services/emailService';
import { PageHeader } from '../components/ui/PageHeader';
import { Card } from '../components/ui/Card';
import { Select } from '../components/ui/Select';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { StatusBadge } from '../components/ui/StatusBadge';
import { Table } from '../components/ui/Table';
import { Pagination } from '../components/ui/Pagination';
import { Modal } from '../components/ui/Modal';
import { Spinner } from '../components/ui/Spinner';
import { EmptyState } from '../components/ui/EmptyState';
import { formatDate, truncate } from '../utils/formatters';

const STATUSES = ['', 'PENDING', 'QUEUED', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED'];

const COLS = [
  { key: 'id',        label: '#',      render: (v) => <span style={{ color: 'var(--gray-400)', fontSize: 12 }}>#{v}</span> },
  { key: 'subject',   label: 'Subject',maxWidth: 200, render: (v) => <strong style={{ fontWeight: 500 }}>{truncate(v, 36)}</strong> },
  { key: 'fromEmail', label: 'From',   render: (v) => <span style={{ fontSize: 13, color: 'var(--gray-500)' }}>{v}</span> },
  { key: 'toEmails',  label: 'To',     render: (v) => <span style={{ fontSize: 13, color: 'var(--gray-500)' }}>{Array.isArray(v) ? v[0] : v}{Array.isArray(v) && v.length > 1 ? ` +${v.length - 1}` : ''}</span> },
  { key: 'status',    label: 'Status', render: (v) => <StatusBadge status={v} /> },
  { key: 'createdAt', label: 'Date',   render: (v) => <span style={{ fontSize: 12, color: 'var(--gray-400)' }}>{formatDate(v)}</span> },
];

export default function EmailHistory() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [emails, setEmails]     = useState([]);
  const [page, setPage]         = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [total, setTotal]       = useState(0);
  const [loading, setLoading]   = useState(true);
  const [detail, setDetail]     = useState(null);
  const [testing, setTesting]   = useState(false);
  const [filters, setFilters]   = useState({ status: searchParams.get('status') || '', from: '', to: '' });

  const load = useCallback(() => {
    setLoading(true);
    const params = { page, size: 15, ...Object.fromEntries(Object.entries(filters).filter(([, v]) => v)) };
    emailApi.list(params).then((r) => {
      setEmails(r.data?.content || []);
      setTotalPages(r.data?.totalPages || 0);
      setTotal(r.data?.totalElements || 0);
    }).finally(() => setLoading(false));
  }, [page, filters]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    const id = searchParams.get('id');
    if (id) emailApi.getById(id).then((r) => setDetail(r.data));
  }, []); 

  const setFilter = (k) => (e) => { setFilters((f) => ({ ...f, [k]: e.target.value })); setPage(0); };

  const handleTest = async (id, fromEmail) => {
    if (!fromEmail) { toast.error('No from address to send the test to'); return; }
    setTesting(id);
    try {
      await emailApi.sendTest(id);
      toast.success(`Test email sent to ${fromEmail}`);
    } finally {
      setTesting(false);
    }
  };

  return (
    <div>
      <PageHeader title="Email History" subtitle={`${total.toLocaleString()} emails`} />

      {/* Filters */}
      <Card style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <div className="form-group" style={{ marginBottom: 0, minWidth: 160 }}>
            <label>Status</label>
            <Select value={filters.status} onChange={setFilter('status')}>
              {STATUSES.map((s) => <option key={s} value={s}>{s || 'All'}</option>)}
            </Select>
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>From</label>
            <Input type="datetime-local" value={filters.from} onChange={setFilter('from')} />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>To</label>
            <Input type="datetime-local" value={filters.to} onChange={setFilter('to')} />
          </div>
          <button className="btn btn-secondary btn-sm" onClick={() => { setFilters({ status: '', from: '', to: '' }); setPage(0); }}>Clear</button>
          <button className="btn btn-primary btn-sm" onClick={load}>Apply</button>
        </div>
      </Card>

      {/* Table */}
      <Card style={{ padding: 0 }}>
        {loading
          ? <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}><Spinner /></div>
          : <>
              <Table columns={COLS} data={emails} onRowClick={(row) => emailApi.getById(row.id).then((r) => setDetail(r.data))}
                emptyState={<EmptyState title="No emails found" description="Try adjusting your filters" />}
              />
              <div style={{ padding: '12px 16px', borderTop: emails.length ? '1px solid var(--gray-200)' : 'none' }}>
                <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
              </div>
            </>
        }
      </Card>

      {/* Detail modal */}
      <Modal open={!!detail} onClose={() => { setDetail(null); setSearchParams({}); }} title="Email Detail" width={580}>
        {detail && (
          <div>
            {[
              ['ID',           `#${detail.id}`],
              ['Status',       null],
              ['From',         detail.fromEmail],
              ['To',           detail.toEmails?.join(', ')],
              ['Subject',      detail.subject],
              ['Template',     detail.templateName],
              ['Created',      formatDate(detail.createdAt)],
              ['Sent At',      formatDate(detail.sentAt)],
              ['Delivered At', formatDate(detail.deliveredAt)],
              ['SG Message ID',detail.sendgridMessageId],
            ].filter(([, v]) => v).map(([label, value]) => (
              <div key={label} style={{ display: 'grid', gridTemplateColumns: '130px 1fr', gap: 8, marginBottom: 10, alignItems: 'flex-start' }}>
                <span style={{ fontSize: 12, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: '0.04em', paddingTop: 1 }}>{label}</span>
                <span style={{ fontSize: 14 }}>
                  {label === 'Status' ? <StatusBadge status={detail.status} /> : value}
                </span>
              </div>
            ))}
            {detail.errorMessage && (
              <div style={{ background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 6, padding: '8px 12px', marginTop: 8, fontSize: 13, color: 'var(--danger)' }}>
                {detail.errorMessage}
              </div>
            )}
            {detail.testEmail && (
              <div style={{ background: '#fef9c3', border: '1px solid #fde047', borderRadius: 6, padding: '6px 12px', marginTop: 8, fontSize: 12, color: '#854d0e' }}>
                🧪 This was a test email — delivered to the from address only.
              </div>
            )}
            {/* Send Test button */}
            <div style={{ marginTop: 16, paddingTop: 14, borderTop: '1px solid var(--gray-200)', display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                variant="secondary"
                size="sm"
                loading={testing === detail.id}
                onClick={() => handleTest(detail.id, detail.fromEmail)}
              >
                Send Test Email
              </Button>
            </div>
            {detail.attachments?.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <div style={{ fontSize: 12, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: 8 }}>Attachments ({detail.attachments.length})</div>
                <div style={{ border: '1px solid var(--gray-200)', borderRadius: 6, overflow: 'hidden' }}>
                  {detail.attachments.map((a) => (
                    <div key={a.id} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '7px 12px', borderBottom: '1px solid var(--gray-100)', fontSize: 13 }}>
                      <span>📎</span>
                      <span style={{ flex: 1 }}>{a.fileName}</span>
                      <span style={{ color: 'var(--gray-400)', fontSize: 12 }}>{(a.fileSize / 1024).toFixed(1)} KB</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
            {detail.body && (
              <div style={{ marginTop: 16 }}>
                <div style={{ fontSize: 12, color: 'var(--gray-400)', textTransform: 'uppercase', letterSpacing: '0.04em', marginBottom: 8 }}>Body</div>
                {detail.html
                  ? <iframe title="body" srcDoc={`<html><body style="font-family:sans-serif;padding:12px">${detail.body}</body></html>`} style={{ width: '100%', height: 220, border: '1px solid var(--gray-200)', borderRadius: 6 }} sandbox="allow-same-origin" />
                  : <pre style={{ background: 'var(--gray-50)', border: '1px solid var(--gray-200)', borderRadius: 6, padding: 12, fontSize: 12, color: 'var(--gray-600)', whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>{detail.body}</pre>
                }
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}