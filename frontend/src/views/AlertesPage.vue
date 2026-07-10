<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Alertes de stock</h1>
      <p class="page-subtitle">Module 12 — Surveillance automatique</p>
    </div>

    <!-- Bannière info -->
    <div class="info-banner">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" style="flex-shrink:0;"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
      Le scheduler vérifie les stocks critiques <strong>chaque heure</strong> et envoie un email récapitulatif aux gestionnaires.
    </div>

    <!-- Stats -->
    <div class="stats-grid" style="grid-template-columns:repeat(3,1fr);margin-bottom:24px;">
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--navy-xlight);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0" stroke="var(--navy)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Total alertes</p><p class="stat-value">{{ alertes.length }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--danger-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M12 9v4M12 17h.01" stroke="var(--danger)" stroke-width="2" stroke-linecap="round"/><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="var(--danger)" stroke-width="1.8"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Critiques</p><p class="stat-value" style="color:var(--danger);">{{ nbCritiques }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--warning-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M12 9v4M12 17h.01" stroke="var(--warning)" stroke-width="2" stroke-linecap="round"/><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="var(--warning)" stroke-width="1.8"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Faibles</p><p class="stat-value" style="color:var(--warning);">{{ nbFaibles }}</p></div>
      </div>
    </div>

    <!-- Tableau -->
    <div class="card">
      <div class="toolbar">
        <div style="display:flex;gap:8px;align-items:center;">
          <label class="form-label" style="margin:0;white-space:nowrap;">Niveau :</label>
          <select class="form-select" style="width:150px;" v-model="filtre" @change="filtrer">
            <option value="TOUS">Tous</option>
            <option value="CRITIQUE">Critique</option>
            <option value="FAIBLE">Faible</option>
          </select>
        </div>
        <div style="display:flex;gap:8px;">
          <button class="btn btn-outline btn-sm" @click="charger" :disabled="chargement">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M4 4v5h.58M20 20v-5h-.58M4.58 9A8 8 0 0120 13.42M19.42 15A8 8 0 014 10.58" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            Actualiser
          </button>
          <button v-if="peutEcrire" class="btn btn-primary btn-sm" @click="confirm.visible = true">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M4 4h16v3l-8 7-8-7V4zM4 11v9h16v-9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
            Envoyer email récap
          </button>
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Produit</th>
              <th>Référence</th>
              <th>Entrepôt</th>
              <th>Zone</th>
              <th style="text-align:right;">Stock dispo</th>
              <th style="text-align:right;">Stock min</th>
              <th>Niveau</th>
              <th>Détection</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement"><td colspan="8" class="empty-state">Chargement...</td></tr>
            <tr v-else-if="alertesFiltrees.length === 0">
              <td colspan="8" class="empty-state">
                <svg width="44" height="44" viewBox="0 0 24 24" fill="none" style="margin-bottom:10px;opacity:.3;display:block;margin-left:auto;margin-right:auto;"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <strong>Aucune alerte active</strong> — tous les stocks sont OK !
              </td>
            </tr>
            <tr v-for="a in alertesFiltrees" :key="a.stockId" :class="{ 'row-critique': a.niveau === 'CRITIQUE' }">
              <td>{{ a.produitNom }}</td>
              <td><code class="ref-code">{{ a.produitReference }}</code></td>
              <td>{{ a.entrepotNom }}</td>
              <td>{{ a.zoneNom || '—' }}</td>
              <td style="text-align:right;font-weight:600;">{{ a.quantiteDisponible }}</td>
              <td style="text-align:right;color:var(--gray-500);">{{ a.stockMin }}</td>
              <td>
                <span class="badge" :class="a.niveau === 'CRITIQUE' ? 'badge-danger badge-pulse' : 'badge-warning'">{{ a.niveau }}</span>
              </td>
              <td>{{ formaterDate(a.dateDetection) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal confirm email -->
    <div class="modal-overlay" v-if="confirm.visible" @click.self="confirm.visible = false">
      <div class="modal" style="max-width:420px;">
        <div class="modal-header">
          <h3 class="modal-title">Envoyer l'email récapitulatif</h3>
          <button class="modal-close" @click="confirm.visible = false">✕</button>
        </div>
        <div class="modal-body">
          <p style="color:var(--gray-700);font-size:.9rem;line-height:1.6;">
            Un email récapitulatif des alertes actives sera envoyé à tous les gestionnaires configurés.
            <br><br><strong>{{ nbCritiques }}</strong> alerte(s) critique(s) et <strong>{{ nbFaibles }}</strong> alerte(s) faible(s) seront incluses.
          </p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="confirm.visible = false">Annuler</button>
          <button class="btn btn-primary" @click="envoyerEmail" :disabled="chargementAction">
            {{ chargementAction ? 'Envoi...' : 'Envoyer l\'email' }}
          </button>
        </div>
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
import { alerteApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'

export default {
  name: 'AlertesPage',
  data() {
    return {
      alertes: [],
      filtre: 'TOUS',
      chargement: true,
      chargementAction: false,
      confirm: { visible: false },
      toast: { visible: false, type: 'succes', message: '', duree: 5000, _timer: null }
    }
  },
  computed: {
    peutEcrire() { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    alertesFiltrees() {
      if (this.filtre === 'TOUS') return this.alertes
      return this.alertes.filter(a => a.niveau === this.filtre)
    },
    nbCritiques() { return this.alertes.filter(a => a.niveau === 'CRITIQUE').length },
    nbFaibles()   { return this.alertes.filter(a => a.niveau === 'FAIBLE').length }
  },
  async mounted() { await this.charger() },
  beforeUnmount() { clearTimeout(this.toast._timer) },
  methods: {
    async charger() {
      this.chargement = true
      try {
        const res = await alerteApi.findAll()
        this.alertes = res.data
      } catch { this.afficherToast('Impossible de charger les alertes.', 'erreur') }
      finally { this.chargement = false }
    },
    filtrer() { /* filtre via computed alertesFiltrees */ },
    async envoyerEmail() {
      this.chargementAction = true
      try {
        await alerteApi.envoyerEmail()
        this.confirm.visible = false
        this.afficherToast('Email récapitulatif envoyé avec succès.', 'succes')
      } catch (e) {
        this.confirm.visible = false
        this.afficherToast(e.response?.data?.message || 'L\'envoi a échoué.', 'erreur')
      } finally { this.chargementAction = false }
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
.info-banner {
  display: flex; align-items: flex-start; gap: 10px;
  background: #eff6ff; border: 1px solid #bfdbfe; border-left: 4px solid var(--blue, #3b82f6);
  border-radius: var(--radius-sm); padding: 12px 16px;
  font-size: .875rem; color: #1e40af; line-height: 1.6;
  margin-bottom: 24px;
}
.empty-state { text-align:center;padding:56px 20px;color:var(--gray-400);font-size:.9rem; }
.ref-code { font-size:.82rem;background:var(--gray-100);padding:2px 6px;border-radius:4px;font-family:monospace; }
.row-critique { background: #fff5f5; }
.badge-pulse {
  animation: pulse-danger 1.6s ease-in-out infinite;
}
@keyframes pulse-danger {
  0%, 100% { box-shadow: 0 0 0 0 rgba(220,38,38,0); }
  50% { box-shadow: 0 0 0 5px rgba(220,38,38,.25); }
}
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
