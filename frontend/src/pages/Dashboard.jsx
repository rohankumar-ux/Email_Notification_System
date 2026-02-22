import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  BarChart, Bar, Cell, LineChart, Line, XAxis, YAxis,
  CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from 'recharts';
import { subDays, format, parseISO, startOfDay, eachDayOfInterval } from 'date-fns';
import { emailApi } from '../services/emailService';
import { StatCard, Card, StatusBadge, PageHeader, Spinner, Table, EmptyState } from '../components/ui/index';
import { formatDate, truncate } from '../utils/formatters';

const STAT_CARDS = [
  { key: 'totalEmails', label: 'Total',     color: 'var(--gray-900)' },
  { key: 'queued',      label: 'Queued',    color: '#0369a1' },
  { key: 'sent',        label: 'Sent',      color: 'var(--primary)' },
  { key: 'delivered',   label: 'Delivered', color: 'var(--success)' },
  { key: 'failed',      label: 'Failed',    color: 'var(--danger)' },
  { key: 'bounced',     label: 'Bounced',   color: 'var(--warning)' },
];

const BAR_COLORS = {
  Delivered: '#16a34a', Sent: '#2563eb', Failed: '#dc2626', Bounced: '#d97706', Queued: '#0369a1',
};

const RECENT_COLS = [
  { key: 'subject',   label: 'Subject',    maxWidth: 200, render: (v) => truncate(v, 36) },
  { key: 'toEmails',  label: 'To',         render: (v) => Array.isArray(v) ? v[0] : v },
  { key: 'status',    label: 'Status',     render: (v) => <StatusBadge status={v} /> },
  { key: 'createdAt', label: 'Date',       render: (v) => <span style={{ color: 'var(--gray-400)', fontSize: 13 }}>{formatDate(v)}</span> },
];

function buildBarData(stats) {
  return ['Delivered', 'Sent', 'Failed', 'Bounced', 'Queued'].map((name) => ({
    name, value: stats[name.toLowerCase()] ?? 0,
  }));
}

function buildLineData(emails) {
  const interval = eachDayOfInterval({ start: subDays(new Date(), 13), end: new Date() });
  const buckets = {};
  interval.forEach((d) => { buckets[format(d, 'yyyy-MM-dd')] = { date: format(d, 'MMM d'), Delivered: 0, Sent: 0, Failed: 0 }; });
  emails.forEach((e) => {
    if (!e.createdAt) return;
    const key = format(startOfDay(parseISO(e.createdAt)), 'yyyy-MM-dd');
    const s = e.status ? e.status.charAt(0) + e.status.slice(1).toLowerCase() : '';
    if (buckets[key] && s in buckets[key]) buckets[key][s]++;
  });
  return Object.values(buckets);
}

const TOOLTIP_STYLE = {
  contentStyle: { fontSize: 13, borderRadius: 6, border: '1px solid var(--gray-200)' },
};

export default function Dashboard() {
  const [stats, setStats]     = useState(null);
  const [recent, setRecent]   = useState([]);
  const [lineData, setLineData] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const load = useCallback(() => {
    setLoading(true);
    Promise.all([
      emailApi.stats(),
      emailApi.list({ page: 0, size: 8 }),
      emailApi.list({ page: 0, size: 200 }),
    ]).then(([s, r, all]) => {
      setStats(s.data);
      setRecent(r.data?.content || []);
      setLineData(buildLineData(all.data?.content || []));
    }).finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  if (loading) return <div style={{ display: 'flex', justifyContent: 'center', padding: 60 }}><Spinner size={28} /></div>;

  return (
    <div>
      <PageHeader
        title="Dashboard"
        actions={<button className="btn btn-ghost btn-sm" onClick={load}>↻ Refresh</button>}
      />

      {/* Stat Cards */}
      <div className="grid-stats" style={{ marginBottom: 20 }}>
        {STAT_CARDS.map(({ key, label, color }) => (
          <StatCard key={key} label={label} value={stats?.[key] ?? 0} color={color}
            onClick={key !== 'totalEmails' ? () => navigate(`/history?status=${key.toUpperCase()}`) : undefined}
          />
        ))}
      </div>

      {/* Charts */}
      <div className="grid-2" style={{ marginBottom: 20 }}>
        <Card>
          <div style={{ fontWeight: 600, marginBottom: 14 }}>Status Distribution</div>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={buildBarData(stats || {})} barCategoryGap="35%">
              <CartesianGrid vertical={false} stroke="var(--gray-100)" />
              <XAxis dataKey="name" tick={{ fontSize: 12, fill: 'var(--gray-500)' }} axisLine={false} tickLine={false} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: 'var(--gray-500)' }} axisLine={false} tickLine={false} width={24} />
              <Tooltip {...TOOLTIP_STYLE} />
              <Bar dataKey="value" name="Count" radius={[4, 4, 0, 0]}>
                {buildBarData(stats || {}).map((entry) => (
                  <Cell key={entry.name} fill={BAR_COLORS[entry.name] || '#6b7280'} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </Card>

        <Card>
          <div style={{ fontWeight: 600, marginBottom: 14 }}>Volume — Last 14 Days</div>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={lineData}>
              <CartesianGrid stroke="var(--gray-100)" strokeDasharray="3 3" />
              <XAxis dataKey="date" tick={{ fontSize: 11, fill: 'var(--gray-500)' }} axisLine={false} tickLine={false} interval={2} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: 'var(--gray-500)' }} axisLine={false} tickLine={false} width={24} />
              <Tooltip {...TOOLTIP_STYLE} />
              <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12 }} />
              <Line type="monotone" dataKey="Delivered" stroke="#16a34a" strokeWidth={2} dot={false} activeDot={{ r: 3 }} />
              <Line type="monotone" dataKey="Sent"      stroke="#2563eb" strokeWidth={2} dot={false} activeDot={{ r: 3 }} />
              <Line type="monotone" dataKey="Failed"    stroke="#dc2626" strokeWidth={2} dot={false} activeDot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        </Card>
      </div>

      {/* Recent Emails */}
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
          <div style={{ fontWeight: 600 }}>Recent Emails</div>
          <button className="btn btn-ghost btn-sm" onClick={() => navigate('/history')}>View all →</button>
        </div>
        <Table
          columns={RECENT_COLS} data={recent}
          onRowClick={(row) => navigate(`/history?id=${row.id}`)}
          emptyState={<EmptyState title="No emails yet" description="Send your first email to get started" />}
        />
      </Card>
    </div>
  );
}