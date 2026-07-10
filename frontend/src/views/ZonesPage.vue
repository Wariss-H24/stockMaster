<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Zones de stockage</h1>
      <p class="page-subtitle">Découpage des entrepôts en zones — Module 3</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher une zone..." />
        </div>
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirModal()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouvelle zone
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Zone</th><th>Entrepôt</th><th>Occupation</th><th>Capacité</th><th>Statut</th>
              <th v-if="peutEcrire || peutSupprimer" style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="6" style="text-align:center;padding:32px;color:var(--gray-400);">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="6" style="text-align:center;padding:32px;color:var(--gray-400);">Aucune zone trouvée.</td>
            </tr>
            <tr v-for="z in listeFiltree" :key="z.id">
              <td><span style="font-weight:600;color:var(--gray-900);">{{ z.nom }}</span></td>
              <td><span class="badge badge-navy">{{ z.entrepotNom }}</span></td>
              <td style="min-width:160px;">
                <div style="display:flex;align-items:center;gap:8px;">
                  <div class="progress-bar">
                    <div class="progress-fill"
                      :class="z.tauxOccupation < 50 ? 'progress-low' : z.tauxOccupation < 80 ? 'progress-mid' : 'progress-high'"
                      :style="{ width: z.tauxOccupation + '%' }">
                    </div>
                  </div>
                  <span style="font-size:.78rem;color:var(--gray-500);min-width:32px;">{{ z.tauxOccupation }}%</span>
                </div>
              </td>
              <td style="font-size:.82rem;color:var(--gray-500);">{{ z.capaciteUtilisee }} / {{ z.capaciteTotale }}</td>
              <td>
                <span class="badge" :class="z.actif ? 'badge-success' : 'badge-danger'">
                  {{ z.actif ? 'Actif' : 'Inactif' }}
                </span>
              </td>
              <td v-if="peutEcrire || peutSupprimer" style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <button v-if="peutEcrire" class="btn btn-outline btn-sm" @click="ouvrirModal(z)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="1.8"/>
                    </svg>
                    Éditer
                  </button>
                  <button v-if="peutSupprimer && z.actif" class="btn btn-danger btn-sm" @click="demanderDesactivation(z)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                    </svg>
                    Désactiver
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

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
          <p class="toast-title">Erreur de capacité</p>
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

    <!-- Modal formulaire -->
    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ form.id ? 'Modifier la zone' : 'Nouvelle zone' }}</h3>
          <button class="modal-close" @click="modal = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Nom de la zone *</label>
            <input class="form-input" v-model="form.nom" placeholder="ex: Zone A - Réception" />
          </div>
          <div class="form-group">
            <label class="form-label">Entrepôt *</label>
            <select class="form-select" v-model.number="form.entrepotId">
              <option value="" disabled>Sélectionner un entrepôt</option>
              <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Capacité totale</label>
              <input class="form-input" type="number" v-model.number="form.capaciteTotale" min="0" />
            </div>
          </div>
          <div class="form-error" v-if="erreur">{{ erreur }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modal = false">Annuler</button>
          <button class="btn btn-primary" @click="sauvegarder">Sauvegarder</button>
        </div>
      </div>
    </div>

    <!-- Modale de confirmation désactivation -->
    <ConfirmModal
      v-if="confirm.visible"
      titre="Désactiver la zone"
      :message="`Voulez-vous désactiver « ${confirm.cible?.nom} » ? Elle ne sera plus visible dans les listes actives.`"
      type="warning"
      label-confirmer="Désactiver"
      @confirmer="confirmerDesactivation"
      @annuler="confirm.visible = false"
    />
  </div>
</template>

<script>
import { zoneApi, entrepotApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'
import ConfirmModal from '../components/ConfirmModal.vue'

export default {
  name: 'ZonesPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], entrepots: [], chargement: true, modal: false, erreur: '', recherche: '',
      confirm: { visible: false, cible: null },
      form: { id: null, nom: '', entrepotId: '', capaciteTotale: 0 },
      toast: { visible: false, message: '', duree: 10000, _timer: null }
    }
  },
  computed: {
    peutEcrire()    { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    peutSupprimer() { return authStore.aRole('ADMIN') },
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(z => z.nom?.toLowerCase().includes(q) || z.entrepotNom?.toLowerCase().includes(q))
    }
  },
  async mounted() {
    this.chargement = true
    try {
      const [z, e] = await Promise.all([zoneApi.findAll(), entrepotApi.findAll()])
      this.liste = z.data
      this.entrepots = e.data
    } finally { this.chargement = false }
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
    ouvrirModal(z = null) {
      this.erreur = ''
      this.form = z
        ? { id: z.id, nom: z.nom, entrepotId: z.entrepotId, capaciteTotale: z.capaciteTotale }
        : { id: null, nom: '', entrepotId: '', capaciteTotale: 0, capaciteUtilisee: 0 }
      this.modal = true
    },
    async sauvegarder() {
      this.erreur = ''
      try {
        if (this.form.id) await zoneApi.modifier(this.form.id, this.form)
        else await zoneApi.creer(this.form)
        this.modal = false
        const r = await zoneApi.findAll(); this.liste = r.data
      } catch (e) {
        const msg = e.response?.data?.message || 'Erreur lors de la sauvegarde.'
        this.erreur = msg
        // Si c'est une erreur de capacité, afficher aussi le toast
        if (msg.includes('Capacité dépassée') || msg.includes('capacit')) {
          this.afficherToast(msg)
        }
      }
    },
    demanderDesactivation(z) {
      this.confirm = { visible: true, cible: z }
    },
    async confirmerDesactivation() {
      await zoneApi.desactiver(this.confirm.cible.id)
      this.confirm = { visible: false, cible: null }
      const r = await zoneApi.findAll(); this.liste = r.data
    }
  }
}
</script>
<style scoped>
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
