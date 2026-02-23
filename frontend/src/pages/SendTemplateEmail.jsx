import { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { emailApi, templateApi } from '../services/emailService';
import { PageHeader } from '../components/ui/PageHeader';
import { Card } from '../components/ui/Card';
import { Field } from '../components/ui/Field';
import { Input } from '../components/ui/Input';
import { TagInput } from '../components/ui/TagInput';
import { Select } from '../components/ui/Select';
import { Button } from '../components/ui/Button';
import { Spinner } from '../components/ui/Spinner';
import { validateEmail, renderTemplate } from '../utils/formatters';

export default function SendTemplateEmail() {
  const [templates, setTemplates]   = useState([]);
  const [selectedId, setSelectedId] = useState('');
  const [selected, setSelected]     = useState(null);
  const [fromEmail, setFromEmail]   = useState('');
  const [toEmails, setToEmails]     = useState([]);
  const [variables, setVariables]   = useState({});
  const [errors, setErrors]         = useState({});
  const [loading, setLoading]       = useState(false);
  const [loadingTpl, setLoadingTpl] = useState(true);
  const [preview, setPreview]       = useState(false);
  const [sentId, setSentId]         = useState(null);

  useEffect(() => {
    templateApi.list(true).then((r) => setTemplates(r.data || [])).finally(() => setLoadingTpl(false));
  }, []);

  useEffect(() => {
    const tpl = templates.find((t) => String(t.id) === String(selectedId));
    setSelected(tpl || null);
    const vars = {};
    tpl?.variableKeys?.forEach((k) => { vars[k] = ''; });
    setVariables(vars);
    setSentId(null);
  }, [selectedId, templates]);

  const validate = () => {
    const e = {};
    if (!selectedId) e.template = 'Please select a template';
    if (!toEmails.length) e.toEmails = 'At least one recipient required';
    else if (toEmails.some((t) => !validateEmail(t))) e.toEmails = 'Invalid email in list';
    if (fromEmail && !validateEmail(fromEmail)) e.fromEmail = 'Invalid email';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSend = async () => {
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await emailApi.sendTemplate({
        fromEmail: fromEmail || undefined, toEmails,
        templateId: Number(selectedId), variables,
      });
      setSentId(res.data?.id);
      toast.success('Template email queued!');
      setToEmails([]); setVariables({}); setFromEmail('');
    } finally {
      setLoading(false);
    }
  };

  if (loadingTpl) return <div style={{ padding: 40, display: 'flex', justifyContent: 'center' }}><Spinner /></div>;

  return (
    <div style={{ maxWidth: 800 }}>
      <PageHeader title="Send Template Email" />

      {sentId && (
        <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: 6, padding: '10px 14px', marginBottom: 16, fontSize: 13, color: 'var(--success)' }}>
          ✓ Email #{sentId} queued for delivery
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: preview && selected ? '1fr 1fr' : '1fr', gap: 16 }}>
        <Card>
          <Field label="Template" required error={errors.template}>
            <Select value={selectedId} onChange={(e) => setSelectedId(e.target.value)} error={errors.template}>
              <option value="">— Select a template —</option>
              {templates.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </Select>
          </Field>

          {selected && (
            <div style={{ background: 'var(--gray-50)', border: '1px solid var(--gray-200)', borderRadius: 6, padding: '8px 12px', marginBottom: 16, fontSize: 13, color: 'var(--gray-600)' }}>
              Subject: {selected.subject}
            </div>
          )}

          <Field label="From Email" hint="Optional" error={errors.fromEmail}>
            <Input type="email" placeholder="sender@yourdomain.com" value={fromEmail} onChange={(e) => setFromEmail(e.target.value)} error={errors.fromEmail} />
          </Field>

          <Field label="To" required error={errors.toEmails} hint="Enter or comma to add">
            <TagInput value={toEmails} onChange={setToEmails} placeholder="recipient@example.com" error={errors.toEmails} />
          </Field>

          {selected?.variableKeys?.length > 0 && (
            <div>
              <div style={{ fontSize: 13, fontWeight: 500, color: 'var(--gray-700)', marginBottom: 10 }}>Template Variables</div>
              {selected.variableKeys.map((key) => (
                <Field key={key} label={`{{${key}}}`}>
                  <Input
                    placeholder={`Value for ${key}`}
                    value={variables[key] || ''}
                    onChange={(e) => setVariables((v) => ({ ...v, [key]: e.target.value }))}
                  />
                </Field>
              ))}
            </div>
          )}

          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            {selected?.htmlBody && (
              <Button variant="secondary" onClick={() => setPreview((p) => !p)}>
                {preview ? 'Hide Preview' : 'Preview'}
              </Button>
            )}
            <Button loading={loading} onClick={handleSend} disabled={!selectedId}>Send</Button>
          </div>
        </Card>

        {preview && selected?.htmlBody && (
          <Card style={{ padding: 0 }}>
            <div style={{ padding: '10px 14px', borderBottom: '1px solid var(--gray-200)', fontSize: 12, color: 'var(--gray-500)' }}>
              Preview
            </div>
            <iframe
              title="preview"
              srcDoc={`<html><body style="font-family:sans-serif;padding:16px">${renderTemplate(selected.htmlBody, variables)}</body></html>`}
              style={{ width: '100%', height: 460, border: 'none' }}
              sandbox="allow-same-origin"
            />
          </Card>
        )}
      </div>
    </div>
  );
}