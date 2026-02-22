import React from 'react';
import { NavLink } from 'react-router-dom';

const NAV = [
  { to: '/',               label: 'Dashboard' },
  { to: '/compose',        label: 'Compose Email' },
  { to: '/send-template',  label: 'Send Template' },
  { to: '/templates',      label: 'Templates' },
  { to: '/history',        label: 'Email History' },
];

export default function Layout({ children }) {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <aside style={{
        width: 'var(--sidebar-w)', flexShrink: 0,
        background: '#fff', borderRight: '1px solid var(--gray-200)',
        display: 'flex', flexDirection: 'column',
        position: 'sticky', top: 0, height: '100vh',
      }}>
        <div style={{ padding: '18px 16px', borderBottom: '1px solid var(--gray-200)', fontWeight: 700, fontSize: 16, color: 'var(--primary)' }}>
          MailFlow
        </div>
        <nav style={{ flex: 1, padding: '8px 8px' }}>
          {NAV.map(({ to, label }) => (
            <NavLink
              key={to}
              to={to}
              style={({ isActive }) => ({
                display: 'block', padding: '8px 10px', borderRadius: 6,
                marginBottom: 2, fontSize: 14,
                color: isActive ? 'var(--primary)' : 'var(--gray-600)',
                background: isActive ? '#eff6ff' : 'transparent',
                fontWeight: isActive ? 500 : 400,
              })}
            >
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main style={{ flex: 1, padding: '24px 28px', overflowY: 'auto' }}>
        {children}
      </main>
    </div>
  );
}