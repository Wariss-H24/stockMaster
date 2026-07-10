<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Traçabilité & Audit</h1>
      <p class="page-subtitle">Module 15 — Historique complet de toutes les actions</p>
    </div>

    <!-- Stats -->
    <div class="stats-grid" style="grid-template-columns:repeat(4,1fr);margin-bottom:24px;">
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--navy-xlight);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="var(--navy)" stroke-width="1.8"/><path d="M12 6v6l4 2" stroke="var(--navy)" stroke-width="1.8" stroke-linecap="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Total logs</p><p class="stat-value">{{ total }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--info-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><rect x="3" y="4" width="18" height="18" rx="2" stroke="var(--info)" stroke-width="1.8"/><path d="M16 2v4M8 2v4M3 10h18" stroke="var(--info)" stroke-width="1.8" stroke-linecap="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Actions aujourd'hui</p><p class="stat-value">{{ stats.aujourd_hui }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--success-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="8" r="4" stroke="var(--success)" stroke-width="1.8"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" stroke="var(--success)" stroke-width="1.8" stroke-linecap="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Utilisateurs actifs</p><p class="stat-value">{{ stats.utilisateurs }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--warning-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M4 6h16M4 12h16M4 18h7" stroke="var(--warning)" stroke-width="1.8" stroke-linecap="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Entités suivies</p><p class="stat-value">{{ stats.entites }}</p></div>
      </div>
    </div>

    <!-- Filtres + Tableau -->
    <div class="card">
      <div class="toolbar" style="flex-wrap:wrap;gap:10px;">
        <div style="display:flex;gap:8px;flex-wrap:wrap;flex:1;min-width:0;">
          <input class="form-input" style="width:160px;" v-model="filtres.entite" placeholder="Entité..." />
          <select class="form-select" style="width:170px;" v-model="filtres.action">
            <option value="">Toutes les actions</option>
            <option value="CREER">CREER</option>
            <option value="MODIFIER">MODIFIER</option>
            <option value="SUPPRIMER">SUPPRIMER</option>
            <option value="VALIDER">VALIDER</option>
            <option value="EXPEDIER">EXPEDIER</option>
            <option value="RECEVOIR">RECEVOIR</option>
            <option value="ANNULER">ANNULER</option>
          </select>
          <input class="form-input" style="width:160px;" v-model="filtres.utilisateur" placeholder="Utilisateur..." />
        </div>
        <button class="btn btn-primary" @click="rechercher" :disabled="chargement">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/><path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          Rechercher
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Utilisateur</th>
              <th>Entité</th>
              <th>Action</th>
              <th>Ancienne valeur</th>
              <th>Nouvelle valeur</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement"><td colspan="6" class="empty-state">Chargement...</td></tr>
            <tr v-else-if="logs.length === 0">
              <td colspan="6" class="empty-state">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" style="display:block;margin:0 auto 8px;opacity:.3;"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z" stroke="currentColor" stroke-width="1.5"/><path d="M12 6v6l4 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                Aucun log trouvé.
              </td>
            </tr>
            <tr v-for="log in logs" :key="log.id">
              <td style="white-space:nowrap;font-size:.82rem;">{{ formaterDate(log.date) }}</td>
              <td>
                <div style="display:flex;align-items:center;gap:6px;">
                  <div class="user-avatar">{{ initiales(log.utilisateur) }}</div>
                  <span style="font-size:.85rem;">{{ log.utilisateur }}</span>
                </div>
              </td>
              <td><span class="badge" :class="badgeEntite(log.entite)">{{ log.entite }}</span></td>
              <td><span class="badge" :class="badgeAction(log.action)">{{ log.action }}</span></td>
              <td>
                <span v-if="log.ancienneValeur" class="val-cell" :title="log.ancienneValeur">
                  {{ tronquer(log.ancienneValeur) }}
                </span>
                <span v-else class="text-muted">—</span>
              </td>
              <td>
                <span v-if="log.nouvelleValeur" class="val-cell" :title="log.nouvelleValeur">
                  {{ tronquer(log.nouvelleValeur) }}
                </span>
                <span v-else class="text-muted">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="nbPages > 0" class="pagination">
        <button class="btn btn-outline btn-sm" @click="chargerPage(page - 1)" :disabled="page === 0 || chargement">
          ← Précédent
        </button>
        <span class="pagination-info">Page {{ page + 1 }} / {{ nbPages }} — {{ total }} log(s)</span>
        <button class="btn btn-outline btn-sm" @click="chargerPage(page + 1)" :disabled="page >= nbPages - 1 || chargement">
          Suivant →
        </button>
      </div>
    </div>

    <!-- Toast -->
    <transition name="toast-slide">
      <div v-if="toast.visible" :class="['toast-notif', toast.type === 'succes' ? 'toast-succes' : 'toast-erreur']">
        <div class="toast-icon">
          <svg v-if="toast.type === 'succes'" width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
        </div>
        <div class="toast-body">
          <p class="toast-title">{{ toast.type === 'succes' ? 'Succès' : 'Erreur' }}</p>
          <p class="toast-message">{{ toast.message }}</p>
          <div class="toast-progress"><div class="toast-progress-bar" :style="{ animationDuration: toast.duree + 'ms' }"></div></div>
        </div>
        <button class="toast-close" @click="fermerToast">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        </button>
      </div>
    </transition>
  </div>
</template>

<script>
import { auditApi } from '../services/api.js'

export default {
  name: 'TracabilitePage',
  data() {
    return {
      logs: [],
      total: 0,
      page: 0,
      size: 50,
      filtres: { entite: '', action: '', utilisateur: '' },
      chargement: true,
      stats: { aujourd_hui: 0, utilisateurs: 0, entites: 0 },
      toast: { visible: false, type: 'succes', message: '', duree: 5000, _timer: null }
    }
  },
  computed: {
    nbPages() { return Math.max(1, Math.ceil(this.total / this.size)) }
  },
  async mounted() { await this.charger() },
  beforeUnmount() { clearTimeout(this.toast._timer) },
  methods: {
    async charger() {
      this.chargement = true
      try {
        const hasFiltre = this.filtres.entite || this.filtres.action || this.filtres.utilisateur
        let res
        if (hasFiltre) {
          res = await auditApi.recherche({ ...this.filtres, page: this.page, size: this.size })
        } else {
          res = await auditApi.findAll(this.page, this.size)
        }
        const data = res.data
        if (data.logs !== undefined) {
          // Format retourné par notre AuditController : { logs, total, page, size }
          this.logs  = data.logs
          this.total = data.total
        } else if (data.content !== undefined) {
          // Format Spring Page standard
          this.logs  = data.content
          this.total = data.totalElements
        } else {
          this.logs  = Array.isArray(data) ? data : []
          this.total = this.logs.length
        }
        this.calculerStats()
      } catch { this.afficherToast('Impossible de charger les logs.', 'erreur') }
      finally { this.chargement = false }
    },
    async chargerPage(n) {
      if (n < 0 || n >= this.nbPages) return
      this.page = n
      await this.charger()
    },
    async rechercher() {
      this.page = 0
      await this.charger()
    },
    calculerStats() {
      const today = new Date().toDateString()
      const logsAujourdhui = this.logs.filter(l => new Date(l.date).toDateString() === today)
      const utilisateurs = new Set(this.logs.map(l => l.utilisateur).filter(Boolean))
      const entites = new Set(this.logs.map(l => l.entite).filter(Boolean))
      this.stats = {
        aujourd_hui: logsAujourdhui.length,
        utilisateurs: utilisateurs.size,
        entites: entites.size
      }
    },
    tronquer(val) {
      if (!val) return ''
      const s = typeof val === 'string' ? val : JSON.stringify(val)
      return s.length > 50 ? s.substring(0, 50) + '…' : s
    },
    initiales(nom) {
      if (!nom) return '?'
      return nom.split(' ').map(p => p[0]).join('').toUpperCase().substring(0, 2)
    },
    badgeAction(action) {
      const map = {
        CREER: 'badge-success', MODIFIER: 'badge-info',
        SUPPRIMER: 'badge-danger', VALIDER: 'badge-navy'
      }
      return map[action] || 'badge-warning'
    },
    badgeEntite(entite) {
      const map = {
        Produit: 'badge-info', Stock: 'badge-navy',
        BonReception: 'badge-warning', BonSortie: 'badge-danger',
        Transfert: 'badge-info', Inventaire: 'badge-navy',
        Utilisateur: 'badge-success', CommandeFournisseur: 'badge-warning'
      }
      return map[entite] || 'badge-navy'
    },
    formaterDate(d) {
      if (!d) return '—'
      const dt = new Date(d)
      return isNaN(dt) ? '—' : dt.toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    },
    afficherToast(message, type = 'succes', duree = 5000) {
      clearTimeout(this.toast._timer)
      this.toast = { visible: true, type, message, duree, _timer: null }
      this.toast._timer = setTimeout(() => { this.toast.visible = false }, duree)
    },
    fermerToast() { clearTimeout(this.toast._timer); this.toast.visible = false }
  }
}
</script>

<style scoped>
.text-muted { color: var(--gray-400); font-size: .82rem; }
.empty-state { text-align:center;padding:48px 20px;color:var(--gray-400);font-size:.9rem; }
.val-cell { font-size:.78rem;font-family:monospace;background:var(--gray-100);padding:2px 5px;border-radius:3px;cursor:default;display:inline-block;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;vertical-align:middle; }
.user-avatar { width:28px;height:28px;border-radius:50%;background:var(--navy-xlight);color:var(--navy);display:flex;align-items:center;justify-content:center;font-size:.7rem;font-weight:700;flex-shrink:0; }
.pagination { display:flex;align-items:center;justify-content:center;gap:16px;padding:16px 20px;border-top:1px solid var(--gray-100); }
.pagination-info { font-size:.84rem;color:var(--gray-600); }
/* Toast */
.toast-notif { position:fixed;top:24px;right:24px;z-index:9999;display:flex;align-items:flex-start;gap:12px;border-radius:var(--radius);box-shadow:var(--shadow-lg);padding:16px 14px 12px 16px;max-width:420px;min-width:300px; }
.toast-succes { background:#f0fdf4;border:1px solid #86efac;border-left:4px solid var(--success); }
.toast-succes .toast-icon,.toast-succes .toast-title { color:var(--success); }
.toast-succes .toast-progress-bar { background:var(--success); }
.toast-succes .toast-progress { background:rgba(22,163,74,.12); }
.toast-erreur { background:#fff5f5;border:1px solid #fca5a5;border-left:4px solid var(--danger); }
.toast-erreur .toast-icon,.toast-erreur .toast-title { color:var(--danger); }
.toast-erreur .toast-progress-bar { background:var(--danger); }
.toast-erreur .toast-progress { background:rgba(220,38,38,.12); }
.toast-icon { flex-shrink:0;margin-top:1px; }
.toast-body { flex:1;min-width:0; }
.toast-title { font-weight:700;font-size:.88rem;margin-bottom:3px; }
.toast-message { font-size:.83rem;color:var(--gray-700);line-height:1.4; }
.toast-progress { margin-top:8px;height:3px;border-radius:999px;overflow:hidden; }
.toast-progress-bar { width:100%;height:100%;border-radius:999px;animation:toast-drain linear forwards; }
.toast-close { background:none;border:none;cursor:pointer;color:var(--gray-400);padding:2px;flex-shrink:0;border-radius:4px;display:flex;align-items:center; }
.toast-close:hover { color:var(--gray-700);background:var(--gray-100); }
@keyframes toast-drain { from { width:100%; } to { width:0%; } }
.toast-slide-enter-active { transition:all .25s ease; }
.toast-slide-leave-active { transition:all .2s ease; }
.toast-slide-enter-from,.toast-slide-leave-to { opacity:0;transform:translateX(40px); }
</style>
