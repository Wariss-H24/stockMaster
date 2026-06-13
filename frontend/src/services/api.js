import axios from 'axios'

// Instance axios avec base URL commune
const api = axios.create({ baseURL: '/api' })

export const utilisateurApi = {
  findAll: () => api.get('/utilisateurs'),
  findById: (id) => api.get(`/utilisateurs/${id}`),
  creer: (dto) => api.post('/utilisateurs', dto),
  modifier: (id, dto) => api.put(`/utilisateurs/${id}`, dto),
  desactiver: (id) => api.delete(`/utilisateurs/${id}`)
}

export const entrepotApi = {
  findAll: () => api.get('/entrepots'),
  findById: (id) => api.get(`/entrepots/${id}`),
  creer: (dto) => api.post('/entrepots', dto),
  modifier: (id, dto) => api.put(`/entrepots/${id}`, dto),
  desactiver: (id) => api.delete(`/entrepots/${id}`)
}

export const zoneApi = {
  findAll: () => api.get('/zones'),
  findById: (id) => api.get(`/zones/${id}`),
  creer: (dto) => api.post('/zones', dto),
  modifier: (id, dto) => api.put(`/zones/${id}`, dto),
  desactiver: (id) => api.delete(`/zones/${id}`)
}

export const produitApi = {
  findAll: () => api.get('/produits'),
  findById: (id) => api.get(`/produits/${id}`),
  creer: (dto) => api.post('/produits', dto),
  modifier: (id, dto) => api.put(`/produits/${id}`, dto),
  supprimer: (id) => api.delete(`/produits/${id}`)
}
