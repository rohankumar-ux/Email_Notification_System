import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { emailApi } from '../services/emailService';
import { PageHeader, Card, Field, Input, Textarea, TagInput, Button } from '../components/ui/index';
import { validateEmail } from '../utils/formatters';

const INIT = { toEmails: [], subject: '', body: '', html: false };

export default function ComposeEmail() {
  const [form, setForm]       = useState(INIT);
  const [errors, setErrors]   = useState({});
  const [sending, setSending] = useState(false);
  const [testing, setTesting] = useState(false);
  // sentEmail holds the last queued email record — needed to fire a test against its id
  const [sentEmail, setSentEmail] = useState(null);

  const set  = (key) => (val) => setForm((f) => ({ ...f, [key]: val }));
  const setE = (key) => (e)   => set(key)(e.target.value);

  const validate = () => {
    const e = {};
    if (!form.toEmails.length)                      e.toEmails  = 'At least one recipient required';
    else if (form.toEmails.some((t) => !validateEmail(t))) e.toEmails = 'One or more emails are invalid';
    if (!form.subject.trim())                        e.subject   = 'Subject is required';
    if (!form.body.trim())                           e.body      = 'Body is required';
    setErrors(e);
    return !Object.keys(e).length;
  };

  const handleSend = async () => {
    if (!validate()) return;
    setSending(true);
    try {
      const res = await emailApi.sendRaw(form);
      setSentEmail(res.data);
      toast.success('Email queued successfully');
      setForm(INIT);
      setErrors({});
    } finally {
      setSending(false);
    }
  };

  const handleTest = async () => {
    // If there's no prior sent email, validate and queue one first, then test it
    if (!sentEmail) {
      if (!validate()) return;
      setSending(true);
      let queued;
      try {
        const res = await emailApi.sendRaw(form);
        queued = res.data;
        setSentEmail(queued);
        setForm(INIT);
        setErrors({});
      } catch {
        setSending(false);
        return;
      }
      setSending(false);
      setTesting(true);
      try {
        await emailApi.sendTest(queued.id);
        toast.success(`Test email sent to ${queued.fromEmail}`);
      } finally {
        setTesting(false);
      }
      return;
    }

    // Already have a queued email — just fire the test
    setTesting(true);
    try {
      await emailApi.sendTest(sentEmail.id);
      toast.success(`Test email sent to ${sentEmail.fromEmail}`);
    } finally {
      setTesting(false);
    }
  };

  const handleReset = () => { setForm(INIT); setErrors({}); setSentEmail(null); };

  return (
    <div style={{ maxWidth: 680 }}>
      <PageHeader title="Compose Email" />

      {sentEmail && (
        <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: 6, padding: '10px 14px', marginBottom: 16, fontSize: 13, color: 'var(--success)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>✓ Email #{sentEmail.id} queued — status: <strong>{sentEmail.status}</strong></span>
          {sentEmail.testEmail && (
            <span style={{ fontSize: 12, background: '#fef9c3', color: '#92400e', border: '1px solid #fde68a', borderRadius: 4, padding: '2px 7px' }}>
              Test sent to {sentEmail.fromEmail}
            </span>
          )}
        </div>
      )}

      <Card>
        <Field label="To" required error={errors.toEmails} hint="Press Enter or comma to add addresses">
          <TagInput value={form.toEmails} onChange={set('toEmails')} placeholder="recipient@example.com" error={errors.toEmails} />
        </Field>

        <Field label="Subject" required error={errors.subject}>
          <Input placeholder="Email subject" value={form.subject} onChange={setE('subject')} error={errors.subject} />
        </Field>

        <Field label="Body type">
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 14, cursor: 'pointer' }}>
            <input type="checkbox" checked={form.html} onChange={(e) => set('html')(e.target.checked)} />
            HTML body
          </label>
        </Field>

        <Field label="Body" required error={errors.body}>
          <Textarea
            rows={10}
            placeholder={form.html ? '<h1>Hello!</h1>' : 'Plain text message...'}
            value={form.body}
            onChange={setE('body')}
            error={errors.body}
            style={form.html ? { fontFamily: 'monospace', fontSize: 13 } : {}}
          />
        </Field>

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', alignItems: 'center' }}>
          <button className="btn btn-ghost btn-sm" onClick={handleReset}>Reset</button>

          <div style={{ flex: 1 }} />

          {/* Test email button — always visible; sends test to the from address */}
          <Button
            variant="secondary"
            loading={testing}
            disabled={sending}
            onClick={handleTest}
          >
            Send Test Email
          </Button>

          <Button loading={sending} disabled={testing} onClick={handleSend}>
            Send Email
          </Button>
        </div>

        <div style={{ marginTop: 10, fontSize: 12, color: 'var(--gray-400)', textAlign: 'right' }}>
          "Send Test" queues the email then immediately delivers a copy to the default sender address with a [TEST] banner.
        </div>
      </Card>
    </div>
  );
}