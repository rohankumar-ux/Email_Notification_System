import api from './api';

export const emailApi = {
  sendRaw:      (data)   => api.post('/emails/send', data).then((r) => r.data),
  sendTemplate: (data)   => api.post('/emails/send-template', data).then((r) => r.data),
  sendTest:     (id)     => api.post(`/emails/${id}/send-test`).then((r) => r.data),
  getById:      (id)     => api.get(`/emails/${id}`).then((r) => r.data),
  list:         (params) => api.get('/emails', { params }).then((r) => r.data),
  stats:        ()       => api.get('/emails/stats').then((r) => r.data),
};


export const templateApi = {
  create:  (data)         => api.post('/templates', data).then((r) => r.data),
  update:  (id, data)     => api.put(`/templates/${id}`, data).then((r) => r.data),
  getById: (id)           => api.get(`/templates/${id}`).then((r) => r.data),
  list:    (activeOnly = false) => api.get('/templates', { params: { activeOnly } }).then((r) => r.data),
  delete:  (id)           => api.delete(`/templates/${id}`).then((r) => r.data),
};