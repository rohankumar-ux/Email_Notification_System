import { useState, useEffect, useCallback } from 'react';
import toast from 'react-hot-toast';
import { templateApi } from '../services/emailService';
import { PageHeader } from '../components/ui/PageHeader';
import { Card } from '../components/ui/Card';
import { Field } from '../components/ui/Field';
import { Input } from '../components/ui/Input';
import { Textarea } from '../components/ui/Textarea';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { StatusBadge } from '../components/ui/StatusBadge';
import { Table } from '../components/ui/Table';
import { EmptyState } from '../components/ui/EmptyState';
import { Spinner } from '../components/ui/Spinner';
import { formatDate } from '../utils/formatters';

const EMPTY = { name: '', subject: '', htmlBody: '', sendgridTemplateId: '', variableKeys: '' };

export default function TemplateManagement() {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading]     = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing]     = useState(null);
  const [form, setForm]           = useState(EMPTY);
  const [errors, setErrors]       = useState({});
  const [saving, setSaving]       = useState(false);
  const [deleteId, setDeleteId]   = useState(null);
  const [preview, setPreview]     = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    templateApi.list().then((r) => setTemplates(r.data || [])).finally(() => setLoading(false));
  }, []);

  useEffect(() => { load(); }, [load]);

  const openCreate = () => { setEditing(null); setForm(EMPTY); setErrors({}); setModalOpen(true); };
  const openEdit = (tpl) => {
    setEditing(tpl);
    setForm({ name: tpl.name, subject: tpl.subject, htmlBody: tpl.htmlBody || '', sendgridTemplateId: tpl.sendgridTemplateId || '', variableKeys: tpl.variableKeys?.join(',') || '' });
    setErrors({}); setModalOpen(true);
  };

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Required';
    if (!form.subject.trim()) e.subject = 'Required';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    const payload = {
      name: form.name.trim(), subject: form.subject.trim(),
      htmlBody: form.htmlBody || undefined,
      sendgridTemplateId: form.sendgridTemplateId || undefined,
      variableKeys: form.variableKeys ? form.variableKeys.split(',').map((k) => k.trim()).filter(Boolean) : [],
    };
    try {
      editing ? await templateApi.update(editing.id, payload) : await templateApi.create(payload);
      toast.success(editing ? 'Template updated' : 'Template created');
      setModalOpen(false); load();
    } finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    await templateApi.delete(id);
    toast.success('Template deactivated');
    setDeleteId(null); load();
  };

  const setF = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const COLS = [
    { key: 'name',     label: 'Name',      render: (v) => <strong>{v}</strong> },
    { key: 'subject',  label: 'Subject',   render: (v) => <span style={{ color: 'var(--gray-500)', fontSize: 13 }}>{v}</span> },
    { key: 'variableKeys', label: 'Variables', render: (v) => v?.length ? v.map((k) => <code key={k} style={{ background: 'var(--gray-100)', borderRadius: 3, padding: '1px 5px', fontSize: 11, marginRight: 4 }}>{`{{${k}}}`}</code>) : '—' },
    { key: 'active',   label: 'Status',    render: (v) => <StatusBadge status={v ? 'DELIVERED' : 'FAILED'} /> },
    { key: 'updatedAt',label: 'Updated',   render: (v) => <span style={{ color: 'var(--gray-400)', fontSize: 13 }}>{formatDate(v)}</span> },
    { key: 'id', label: '', render: (_, row) => (
      <div style={{ display: 'flex', gap: 6 }} onClick={(e) => e.stopPropagation()}>
        {row.htmlBody && <button className="btn btn-ghost btn-sm" onClick={() => setPreview(row)}>Preview</button>}
        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(row)}>Edit</button>
        {row.active && <button className="btn btn-danger btn-sm" onClick={() => setDeleteId(row.id)}>Delete</button>}
      </div>
    )},
  ];

  return (
    <div>
      <PageHeader title="Templates" actions={<Button onClick={openCreate}>+ New Template</Button>} />

      <Card style={{ padding: 0 }}>
        {loading
          ? <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}><Spinner /></div>
          : <Table columns={COLS} data={templates}
              emptyState={<EmptyState title="No templates" description="Create a reusable template with {{placeholder}} variables" action={<Button onClick={openCreate}>Create Template</Button>} />}
            />
        }
      </Card>

      {/* Create / Edit */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? `Edit: ${editing.name}` : 'New Template'} width={600}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <Field label="Name" required error={errors.name}>
            <Input placeholder="welcome_email" value={form.name} onChange={setF('name')} error={errors.name} />
          </Field>
          <Field label="SendGrid Template ID" hint="Optional">
            <Input placeholder="d-xxxxxxxxxx" value={form.sendgridTemplateId} onChange={setF('sendgridTemplateId')} style={{ fontFamily: 'monospace', fontSize: 12 }} />
          </Field>
        </div>
        <Field label="Subject" required error={errors.subject}>
          <Input placeholder="Hello {{name}}!" value={form.subject} onChange={setF('subject')} error={errors.subject} />
        </Field>
        <Field label="Variable Keys" hint="Comma-separated — e.g. name,orderId">
          <Input placeholder="name,orderId,amount" value={form.variableKeys} onChange={setF('variableKeys')} style={{ fontFamily: 'monospace', fontSize: 12 }} />
        </Field>
        <Field label="HTML Body" hint="Use {{variable}} for placeholders">
          <Textarea rows={9} placeholder={'<h1>Hello {{name}}!</h1>'} value={form.htmlBody} onChange={setF('htmlBody')} style={{ fontFamily: 'monospace', fontSize: 12 }} />
        </Field>
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button variant="secondary" onClick={() => setModalOpen(false)}>Cancel</Button>
          <Button loading={saving} onClick={handleSave}>{editing ? 'Save Changes' : 'Create'}</Button>
        </div>
      </Modal>

      {/* Confirm delete */}
      <Modal open={!!deleteId} onClose={() => setDeleteId(null)} title="Deactivate Template" width={380}>
        <p style={{ color: 'var(--gray-600)', marginBottom: 20, fontSize: 14 }}>This template will be deactivated. Historical emails are unaffected.</p>
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <Button variant="secondary" onClick={() => setDeleteId(null)}>Cancel</Button>
          <Button variant="danger" onClick={() => handleDelete(deleteId)}>Deactivate</Button>
        </div>
      </Modal>

      {/* Preview */}
      <Modal open={!!preview} onClose={() => setPreview(null)} title={`Preview: ${preview?.name}`} width={660}>
        {preview?.htmlBody && (
          <iframe title="preview" srcDoc={`<html><body style="font-family:sans-serif;padding:16px">${preview.htmlBody}</body></html>`}
            style={{ width: '100%', height: 380, border: '1px solid var(--gray-200)', borderRadius: 6 }} sandbox="allow-same-origin" />
        )}
      </Modal>
    </div>
  );
}