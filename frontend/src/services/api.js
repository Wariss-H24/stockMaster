import axios from 'axios'
import { authStore } from './authStore'

const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use(config => {
  if (authStore.token) config.headers.Authorization = `Bearer ${authStore.token}`
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      authStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const authApi = {
  login:    (dto) => api.post('/auth/login', dto),
  register: (dto) => api.post('/auth/register', dto)
}

export const utilisateurApi = {
  findAll:    ()        => api.get('/utilisateurs'),
  findById:   (id)      => api.get(`/utilisateurs/${id}`),
  creer:      (dto)     => api.post('/utilisateurs', dto),
  modifier:   (id, dto) => api.put(`/utilisateurs/${id}`, dto),
  desactiver: (id)      => api.delete(`/utilisateurs/${id}/desactiver`),
  supprimer:  (id)      => api.delete(`/utilisateurs/${id}`)
}

export const entrepotApi = {
  findAll:    ()        => api.get('/entrepots'),
  findById:   (id)      => api.get(`/entrepots/${id}`),
  creer:      (dto)     => api.post('/entrepots', dto),
  modifier:   (id, dto) => api.put(`/entrepots/${id}`, dto),
  desactiver: (id)      => api.delete(`/entrepots/${id}`)
}

export const zoneApi = {
  findAll:    ()        => api.get('/zones'),
  findById:   (id)      => api.get(`/zones/${id}`),
  creer:      (dto)     => api.post('/zones', dto),
  modifier:   (id, dto) => api.put(`/zones/${id}`, dto),
  desactiver: (id)      => api.delete(`/zones/${id}`)
}

export const produitApi = {
  findAll:  ()        => api.get('/produits'),
  findById: (id)      => api.get(`/produits/${id}`),
  creer:    (dto)     => api.post('/produits', dto),
  modifier: (id, dto) => api.put(`/produits/${id}`, dto),
  supprimer:(id)      => api.delete(`/produits/${id}`)
}

export const categoryApi = {
  findAll:    ()        => api.get('/categories'),
  findById:   (id)      => api.get(`/categories/${id}`),
  creer:      (dto)     => api.post('/categories', dto),
  modifier:   (id, dto) => api.put(`/categories/${id}`, dto),
  desactiver: (id)      => api.delete(`/categories/${id}`)
}

export const fournisseurApi = {
  findAll:    ()        => api.get('/fournisseurs'),
  findById:   (id)      => api.get(`/fournisseurs/${id}`),
  livraisons: (id)      => api.get(`/fournisseurs/${id}/livraisons`),
  creer:      (dto)     => api.post('/fournisseurs', dto),
  modifier:   (id, dto) => api.put(`/fournisseurs/${id}`, dto),
  desactiver: (id)      => api.delete(`/fournisseurs/${id}`)
}

export const stockApi = {
  findAll:   ()                       => api.get('/stocks'),
  findById:  (id)                     => api.get(`/stocks/${id}`),
  majSeuils: (id, stockMin, stockMax) => api.patch(`/stocks/${id}/seuils`, { stockMin, stockMax })
}

export const mouvementStockApi = {
  findAll:      ()       => api.get('/mouvements-stock'),
  findByType:   (type)   => api.get(`/mouvements-stock/type/${type}`),
  findByStockId:(stockId)=> api.get(`/mouvements-stock/stock/${stockId}`)
}

export const receptionApi = {
  findAll:  ()        => api.get('/bon-receptions'),
  findById: (id)      => api.get(`/bon-receptions/${id}`),
  creer:    (dto)     => api.post('/bon-receptions', dto),
  modifier: (id, dto) => api.put(`/bon-receptions/${id}`, dto),
  valider:  (id)      => api.post(`/bon-receptions/${id}/valider`),
  supprimer:(id)      => api.delete(`/bon-receptions/${id}`)
}

export const sortieApi = {
  findAll:  ()        => api.get('/bon-sorties'),
  findById: (id)      => api.get(`/bon-sorties/${id}`),
  creer:    (dto)     => api.post('/bon-sorties', dto),
  modifier: (id, dto) => api.put(`/bon-sorties/${id}`, dto),
  valider:  (id)      => api.post(`/bon-sorties/${id}/valider`),
  supprimer:(id)      => api.delete(`/bon-sorties/${id}`)
}

export const transfertApi = {
  findAll:  ()   => api.get('/transferts'),
  findById: (id) => api.get(`/transferts/${id}`),
  creer:    (dto) => api.post('/transferts', dto),
  expedier: (id) => api.patch(`/transferts/${id}/expedier`),
  recevoir: (id) => api.patch(`/transferts/${id}/recevoir`),
  annuler:  (id) => api.patch(`/transferts/${id}/annuler`)
}

export const inventaireApi = {
  findAll:          ()            => api.get('/inventaires'),
  findById:         (id)          => api.get(`/inventaires/${id}`),
  creer:            (dto)         => api.post('/inventaires', dto),
  mettreAJourLignes:(id, lignes)  => api.patch(`/inventaires/${id}/lignes`, lignes),
  cloturer:         (id)          => api.patch(`/inventaires/${id}/cloturer`),
  annuler:          (id)          => api.patch(`/inventaires/${id}/annuler`)
}

export const alerteApi = {
  findAll:      () => api.get('/alertes'),
  critiques:    () => api.get('/alertes/critiques'),
  envoyerEmail: () => api.post('/alertes/envoyer-email')
}

export const dashboardApi = {
  kpis: () => api.get('/dashboard/kpis')
}

export const reportingApi = {
  pdfStock:         () => api.get('/reporting/pdf/stock',       { responseType: 'blob' }),
  pdfMouvements:    () => api.get('/reporting/pdf/mouvements',  { responseType: 'blob' }),
  excelStock:       () => api.get('/reporting/excel/stock',     { responseType: 'blob' }),
  excelMouvements:  () => api.get('/reporting/excel/mouvements',{ responseType: 'blob' }),
  csvStock:         () => api.get('/reporting/csv/stock',       { responseType: 'blob' }),
  csvMouvements:    () => api.get('/reporting/csv/mouvements',  { responseType: 'blob' })
}

export const auditApi = {
  findAll:   (page = 0, size = 50) => api.get(`/audit?page=${page}&size=${size}`),
  recherche: (params)              => api.get('/audit/recherche', { params }),
  parEntite: (entite, id)          => api.get(`/audit/entite/${entite}/${id}`)
}

export const emplacementApi = {
  findAll:       ()            => api.get('/emplacements'),
  findByZone:    (zoneId)      => api.get(`/emplacements/zone/${zoneId}`),
  findByEntrepot:(entrepotId)  => api.get(`/emplacements/entrepot/${entrepotId}`),
  disponibles:   ()            => api.get('/emplacements/disponibles'),
  findById:      (id)          => api.get(`/emplacements/${id}`),
  creer:         (dto)         => api.post('/emplacements', dto),
  modifier:      (id, dto)     => api.put(`/emplacements/${id}`, dto),
  supprimer:     (id)          => api.delete(`/emplacements/${id}`)
}

export const qrCodeApi = {
  produit:      (id) => api.get(`/qrcode/produit/${id}`),
  emplacement:  (id) => api.get(`/qrcode/emplacement/${id}`)
}

export const commandeApi = {
  findAll:      ()            => api.get('/commandes-fournisseurs'),
  findById:     (id)          => api.get(`/commandes-fournisseurs/${id}`),
  creer:        (dto)         => api.post('/commandes-fournisseurs', dto),
  modifier:     (id, dto)     => api.put(`/commandes-fournisseurs/${id}`, dto),
  envoyer:      (id)          => api.patch(`/commandes-fournisseurs/${id}/envoyer`),
  receptionner: (id)          => api.patch(`/commandes-fournisseurs/${id}/receptionner`),
  annuler:      (id)          => api.patch(`/commandes-fournisseurs/${id}/annuler`),
  supprimer:    (id)          => api.delete(`/commandes-fournisseurs/${id}`)
}
