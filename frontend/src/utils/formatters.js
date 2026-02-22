import { format, parseISO } from 'date-fns';

export const formatDate = (dateStr) => {
  if (!dateStr) return '—';
  try {
    return format(parseISO(dateStr), 'MMM d, yyyy HH:mm');
  } catch {
    return dateStr;
  }
};

export const truncate = (str, n = 40) =>
  str && str.length > n ? str.slice(0, n) + '…' : str || '';

export const validateEmail = (email) =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email).trim());

/**
 * Replaces {{key}} placeholders in a template string with values from a map.
 */
export const renderTemplate = (body, variables = {}) => {
  if (!body) return '';
  return body.replace(/\{\{(\w+)\}\}/g, (_, key) => variables[key] || `{{${key}}}`);
};
