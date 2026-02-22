import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import Layout from './components/layout/Layout';
import Dashboard from './pages/Dashboard';
import ComposeEmail from './pages/ComposeEmail';
import SendTemplateEmail from './pages/SendTemplateEmail';
import TemplateManagement from './pages/TemplateManagement';
import EmailHistory from './pages/EmailHistory';

export default function App() {
  return (
    <BrowserRouter>
      <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
      <Layout>
        <Routes>
          <Route path="/"              element={<Dashboard />} />
          <Route path="/compose"       element={<ComposeEmail />} />
          <Route path="/send-template" element={<SendTemplateEmail />} />
          <Route path="/templates"     element={<TemplateManagement />} />
          <Route path="/history"       element={<EmailHistory />} />
          <Route path="*"              element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}