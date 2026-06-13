import axios from 'axios'
import { authStore } from './authStore'

// Instance axios avec base URL commune
const api = axios.create({ baseURL: '/api' })

// Intercepteur : injecte automatiquement le token JWT dans chaque requête
api.interceptors.request.use(config => {
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  // LOG : affiche exactement ce qui part vers le backend
  console.log('[API REQUEST]', config.method?.toUpperCase(), config.baseURL + config.url)
  console.log('[API REQUEST] body:', JSON.stringify(config.data))
  console.log('[API REQUEST] headers:', JSON.stringify(config.headers))
  return config
})

// Intercepteur réponse : déconnexion automatique si 401 (token expiré)
api.interceptors.response.use(
  res => {
    console.log('[API RESPONSE]', res.status, res.config.url)
    return res
  },
  err => {
    console.error('[API ERROR]', err.response?.status, err.config?.url)
    console.error('[API ERROR] data:', JSON.stringify(err.response?.data))
    if (err.response?.status === 401) {
      authStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const authApi = {
  login: (dto) => api.post('/auth/login', dto),
  register: (dto) => api.post('/auth/register', dto)
}

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
