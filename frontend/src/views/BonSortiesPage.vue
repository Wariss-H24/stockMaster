<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Bons de sortie</h1>
      <p class="page-subtitle">Suivi des expéditions et bordereaux de sortie — Module 9</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher par destination, entrepôt, statut..." />
        </div>
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirModal()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouveau bon
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Destination</th><th>Entrepôt</th><th>Statut</th><th>Bordereau</th><th>Date</th><th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="6" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="6" class="empty-state">Aucun bon de sortie trouvé.</td>
            </tr>
            <tr v-for="b in listeFiltree" :key="b.id">
              <td>{{ b.destination }}</td>
              <td>{{ b.entrepotNom || b.entrepotId }}</td>
              <td>
                <span class="badge" :class="b.statut === 'VALIDE' ? 'badge-success' : 'badge-warning'">{{ b.statut }}</span>
              </td>
              <td>{{ b.bordereauReference || '—' }}</td>
              <td>{{ formaterDate(b.date) }}</td>
              <td style="text-align:right;">
                <button class="btn btn-outline btn-sm" @click="ouvrirModal(b)">Voir</button>
                <button v-if="peutEcrire && b.statut === 'BROUILLON'" class="btn btn-primary btn-sm" @click="valider(b)">Valider</button>
                <button v-if="peutSupprimer && b.statut === 'BROUILLON'" class="btn btn-danger btn-sm" @click="demanderSuppression(b)">Supprimer</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <ConfirmModal
      v-if="confirm.visible"
      titre="Supprimer le bon de sortie"
      :message="`Voulez-vous supprimer le bon #${confirm.cible?.id} (${confirm.cible?.destination}) ? Cette action est irréversible.`"
      type="danger"
      label-confirmer="Supprimer"
      @confirmer="confirmerSuppression"
      @annuler="confirm.visible = false"
    />

    <!-- Toast erreur auto-fermant -->
    <transition name="toast-slide">
      <div v-if="toast.visible" class="toast-error">
        <div class="toast-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="toast-body">
          <p class="toast-title">Erreur</p>
          <p class="toast-message">{{ toast.message }}</p>
          <div class="toast-progress">
            <div class="toast-progress-bar" :style="{ animationDuration: toast.duree + 'ms' }"></div>
          </div>
        </div>
        <button class="toast-close" @click="fermerToast">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </transition>

    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal large-modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ form.id ? 'Modifier le bon de sortie' : 'Nouveau bon de sortie' }}</h3>
          <button class="modal-close" @click="modal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Entrepôt *</label>
              <select class="form-select" v-model.number="form.entrepotId">
                <option value="">Sélectionner</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Destination *</label>
              <input class="form-input" v-model="form.destination" placeholder="Ex: Client X / Site Y" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Commentaire</label>
            <textarea class="form-input" v-model="form.commentaire" rows="3" placeholder="Notes de sortie"></textarea>
          </div>
          <div class="line-table">
            <div class="line-header">Lignes de sortie</div>
            <div v-if="form.lignes.length === 0" class="empty-state">Ajoutez au moins une ligne de produit.</div>
            <div v-for="(ligne, index) in form.lignes" :key="index" class="line-row">
              <div class="form-row align-end">
                <div class="form-group">
                  <label class="form-label">Produit *</label>
                  <select class="form-select" v-model.number="ligne.produitId">
                    <option value="">Sélectionner</option>
                    <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.nom }}</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Quantité *</label>
                  <input class="form-input" type="number" min="1" v-model.number="ligne.quantite" />
                </div>
                <!-- Emplacement affiché depuis le stock (info magasinier) -->
                <div v-if="ligne.emplacementCodeComplet" class="form-group" style="min-width:0;">
                  <label class="form-label">Aller chercher à</label>
                  <div class="emp-info-badge">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" stroke="currentColor" stroke-width="2"/></svg>
                    {{ ligne.emplacementCodeComplet }}
                  </div>
                </div>
                <button class="btn btn-danger btn-sm" style="height:36px;align-self:flex-end;" @click.prevent="supprimerLigne(index)">Supprimer</button>
              </div>
            </div>
            <button class="btn btn-outline" @click.prevent="ajouterLigne">Ajouter une ligne</button>
          </div>
          <div class="form-error" v-if="erreur">{{ erreur }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modal = false">Annuler</button>
          <button class="btn btn-primary" @click="sauvegarder">Sauvegarder</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { sortieApi, entrepotApi, produitApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'
import ConfirmModal from '../components/ConfirmModal.vue'

export default {
  name: 'BonSortiesPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], entrepots: [], produits: [], recherche: '', chargement: true,
      modal: false, erreur: '',
      form: { id: null, entrepotId: '', destination: '', commentaire: '', lignes: [] },
      confirm: { visible: false, cible: null },
      toast: { visible: false, message: '', duree: 10000, _timer: null }
    }
  },
  computed: {
    peutEcrire()    { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER') },
    peutSupprimer() { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(b =>
        b.destination?.toLowerCase().includes(q) ||
        b.entrepotNom?.toLowerCase().includes(q) ||
        b.bordereauReference?.toLowerCase().includes(q) ||
        b.statut?.toLowerCase().includes(q)
      )
    }
  },
  async mounted() {
    await Promise.all([this.charger(), this.chargerDonnees()])
  },
  beforeUnmount() {
    clearTimeout(this.toast._timer)
  },
  methods: {
    afficherToast(message, duree = 10000) {
      clearTimeout(this.toast._timer)
      this.toast = { visible: true, message, duree, _timer: null }
      this.toast._timer = setTimeout(() => { this.toast.visible = false }, duree)
    },
    fermerToast() {
      clearTimeout(this.toast._timer)
      this.toast.visible = false
    },
    async charger() {
      this.chargement = true
      try { const res = await sortieApi.findAll(); this.liste = res.data } finally { this.chargement = false }
    },
    async chargerDonnees() {
      const [e, p] = await Promise.all([entrepotApi.findAll(), produitApi.findAll()])
      this.entrepots = e.data
      this.produits = p.data
    },
    ouvrirModal(b = null) {
      this.erreur = ''
      if (b) {
        this.form = {
          id:          b.id,
          entrepotId:  b.entrepotId,
          destination: b.destination,
          commentaire: b.commentaire || '',
          lignes: (b.lignes || []).map(l => ({
            produitId:             l.produitId,
            quantite:              l.quantite,
            emplacementCodeComplet: l.emplacementCodeComplet || null  // lecture seule, affiché seulement
          }))
        }
      } else {
        this.form = { id: null, entrepotId: '', destination: '', commentaire: '', lignes: [] }
      }
      this.modal = true
    },
    ajouterLigne() {
      this.form.lignes.push({ produitId: '', quantite: 1 })
    },
    supprimerLigne(index) {
      this.form.lignes.splice(index, 1)
    },
    async sauvegarder() {
      this.erreur = ''
      try {
        if (!this.form.entrepotId) throw new Error('L’entrepôt est requis.')
        if (!this.form.destination?.trim()) throw new Error('La destination est requise.')
        if (this.form.lignes.length === 0) throw new Error('Ajoutez au moins une ligne de sortie.')
        if (this.form.lignes.some(l => !l.produitId || l.quantite < 1)) {
          throw new Error('Chaque ligne doit contenir un produit et une quantité valide.')
        }
        if (this.form.id) await sortieApi.modifier(this.form.id, {
            entrepotId: this.form.entrepotId, destination: this.form.destination,
            commentaire: this.form.commentaire || '',
            lignes: this.form.lignes.map(l => ({ produitId: l.produitId, quantite: l.quantite }))
          })
        else await sortieApi.creer({
            entrepotId: this.form.entrepotId, destination: this.form.destination,
            commentaire: this.form.commentaire || '',
            lignes: this.form.lignes.map(l => ({ produitId: l.produitId, quantite: l.quantite }))
          })
        this.modal = false
        await this.charger()
      } catch (e) {
        this.erreur = e.response?.data?.message || e.message || 'Impossible de sauvegarder le bon.'
      }
    },
    async valider(bon) {
      try {
        await sortieApi.valider(bon.id)
        await this.charger()
      } catch (e) {
        const msg = e.response?.data?.message || 'Impossible de valider le bon.'
        this.afficherToast(msg)
      }
    },
    demanderSuppression(bon) {
      this.confirm = { visible: true, cible: bon }
    },
    async confirmerSuppression() {
      try {
        await sortieApi.supprimer(this.confirm.cible.id)
        this.confirm = { visible: false, cible: null }
        await this.charger()
      } catch (e) {
        this.confirm = { visible: false, cible: null }
        this.afficherToast(e.response?.data?.message || 'Impossible de supprimer le bon.')
      }
    },
    formaterDate(date) {
      if (!date) return '—'
      const d = new Date(date)
      return isNaN(d) ? '—' : d.toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    }
  }
}
</script>

<style scoped>
.empty-state { text-align:center;padding:32px;color:var(--gray-400); }
.large-modal { max-width: 860px; }
.line-table { margin-top: 20px; border: 1px solid var(--gray-200); border-radius: var(--radius-sm); padding: 18px; background: var(--gray-50); }
.line-header { font-weight: 700; margin-bottom: 14px; color: var(--gray-700); }
.line-row { margin-bottom: 14px; padding-bottom: 14px; border-bottom: 1px solid var(--gray-200); }
.line-row:last-child { margin-bottom: 0; border-bottom: none; }
.align-end { align-items: flex-end; }
.emp-info-badge {
  display: flex; align-items: center; gap: 5px;
  background: #f0fdf4; border: 1px solid #86efac;
  color: #15803d; border-radius: 6px;
  padding: 7px 10px; font-size: .78rem; font-family: monospace; font-weight: 600;
}

  /* ── Toast erreur ── */
  .toast-error {
    position: fixed;
    top: 24px;
    right: 24px;
    z-index: 9999;
    display: flex;
    align-items: flex-start;
    gap: 12px;
    background: #fff;
    border: 1px solid #fca5a5;
    border-left: 4px solid var(--danger);
    border-radius: var(--radius);
    box-shadow: var(--shadow-lg);
    padding: 16px 14px 12px 16px;
    max-width: 420px;
    min-width: 300px;
  }
  .toast-icon { color: var(--danger); flex-shrink: 0; margin-top: 1px; }
  .toast-body { flex: 1; min-width: 0; }
  .toast-title { font-weight: 700; font-size: .88rem; color: var(--danger); margin-bottom: 3px; }
  .toast-message { font-size: .83rem; color: var(--gray-700); line-height: 1.4; }
  .toast-close {
    background: none; border: none; cursor: pointer;
    color: var(--gray-400); padding: 2px; flex-shrink: 0;
    border-radius: 4px; display:flex; align-items:center;
  }
  .toast-close:hover { color: var(--gray-700); background: var(--gray-100); }
  .toast-progress { margin-top: 8px; height: 3px; background: var(--danger-bg); border-radius: 999px; overflow: hidden; }
  .toast-progress-bar {
    height: 100%;
    background: var(--danger);
    border-radius: 999px;
    width: 100%;
    animation: toast-drain linear forwards;
  }
  @keyframes toast-drain { from { width: 100%; } to { width: 0%; } }
  .toast-slide-enter-active { transition: all .25s ease; }
  .toast-slide-leave-active { transition: all .2s ease; }
  .toast-slide-enter-from { opacity: 0; transform: translateX(40px); }
  .toast-slide-leave-to   { opacity: 0; transform: translateX(40px); }
</style>
