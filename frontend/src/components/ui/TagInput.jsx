import { useState, useRef } from 'react';

export function TagInput({ value = [], onChange, placeholder, error }) {
  const [input, setInput] = useState('');
  const ref = useRef();

  const add = (raw) => {
    const tags = raw.split(/[\s,;]+/).map((t) => t.trim()).filter(Boolean);
    onChange([...new Set([...value, ...tags])]);
    setInput('');
  };
  const remove = (i) => onChange(value.filter((_, idx) => idx !== i));

  const onKey = (e) => {
    if (['Enter', ',', 'Tab'].includes(e.key)) { e.preventDefault(); if (input.trim()) add(input); }
    else if (e.key === 'Backspace' && !input && value.length) remove(value.length - 1);
  };

  return (
    <div
      onClick={() => ref.current?.focus()}
      className={`form-control${error ? ' error' : ''}`}
      style={{ display: 'flex', flexWrap: 'wrap', gap: 4, minHeight: 38, cursor: 'text', padding: '4px 8px' }}
    >
      {value.map((tag, i) => (
        <span key={i} style={{ display: 'inline-flex', alignItems: 'center', gap: 4, background: '#eff6ff', color: 'var(--primary)', borderRadius: 4, padding: '1px 6px', fontSize: 12 }}>
          {tag}
          <button onClick={(e) => { e.stopPropagation(); remove(i); }} style={{ color: 'var(--gray-400)', lineHeight: 1, fontSize: 14 }}>×</button>
        </span>
      ))}
      <input
        ref={ref}
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyDown={onKey}
        onBlur={() => { if (input.trim()) add(input); }}
        onPaste={(e) => { e.preventDefault(); add(e.clipboardData.getData('text')); }}
        placeholder={value.length === 0 ? placeholder : ''}
        style={{ border: 'none', outline: 'none', background: 'transparent', fontSize: 14, flex: 1, minWidth: 100, padding: '2px 0' }}
      />
    </div>
  );
}