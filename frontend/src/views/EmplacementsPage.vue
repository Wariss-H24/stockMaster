<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Emplacements</h1>
      <p class="page-subtitle">Module 17 — Structure : Entrepôt › Zone › Rayon › Étagère › Emplacement</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher par code, zone, rayon, étagère..." />
        </div>
        <div style="display:flex;gap:8px;">
          <select class="form-select" v-model="filtreZone" style="width:180px;">
            <option value="">Toutes les zones</option>
            <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.entrepotNom }} — {{ z.nom }}</option>
          </select>
          <button class="btn btn-primary" @click="ouvrirModal()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
            </svg>
            Nouvel emplacement
          </button>
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Code complet</th>
              <th>Entrepôt</th>
              <th>Zone</th>
              <th>Rayon</th>
              <th>Étagère</th>
              <th>Code</th>
              <th>Statut</th>
              <th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement"><td colspan="8" class="empty-state">Chargement...</td></tr>
            <tr v-else-if="listeFiltree.length === 0"><td colspan="8" class="empty-state">Aucun emplacement trouvé.</td></tr>
            <tr v-for="e in listeFiltree" :key="e.id">
              <td><code class="ref-code">{{ e.codeComplet }}</code></td>
              <td>{{ e.entrepotNom }}</td>
              <td>{{ e.zoneNom }}</td>
              <td>{{ e.rayon }}</td>
              <td>{{ e.etagere }}</td>
              <td>{{ e.code }}</td>
              <td>
                <span class="badge" :class="e.occupe ? 'badge-danger' : 'badge-success'">
                  {{ e.occupe ? 'Occupé' : 'Libre' }}
                </span>
              </td>
              <td style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <button class="btn btn-outline btn-sm" @click="ouvrirQr(e)">QR</button>
                  <button class="btn btn-outline btn-sm" @click="ouvrirModal(e)">Modifier</button>
                  <button v-if="!e.occupe" class="btn btn-danger btn-sm" @click="demanderSuppression(e)">Supprimer</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal formulaire -->
    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ form.id ? 'Modifier l\'emplacement' : 'Nouvel emplacement' }}</h3>
          <button class="modal-close" @click="modal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Zone *</label>
            <select class="form-select" v-model.number="form.zoneId">
              <option value="">Sélectionner</option>
              <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.entrepotNom }} — {{ z.nom }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Rayon *</label>
              <input class="form-input" v-model="form.rayon" placeholder="ex: RAYON-03" />
            </div>
            <div class="form-group">
              <label class="form-label">Étagère *</label>
              <input class="form-input" v-model="form.etagere" placeholder="ex: ETAGERE-02" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Code *</label>
              <input class="form-input" v-model="form.code" placeholder="ex: EMP-12" />
            </div>
            <div class="form-group">
              <label class="form-label">Statut</label>
              <label class="form-check">
                <input type="checkbox" v-model="form.occupe" />
                <span>Occupé</span>
              </label>
            </div>
          </div>
          <div class="form-error" v-if="erreur">{{ erreur }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modal = false">Annuler</button>
          <button class="btn btn-primary" @click="sauvegarder" :disabled="chargementAction">
            {{ chargementAction ? 'Sauvegarde...' : 'Sauvegarder' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal QR Code -->
    <div class="modal-overlay" v-if="modalQr" @click.self="modalQr = false">
      <div class="modal" style="max-width:360px;text-align:center;">
        <div class="modal-header">
          <h3 class="modal-title">QR Code</h3>
          <button class="modal-close" @click="modalQr = false">✕</button>
        </div>
        <div class="modal-body">
          <p style="font-size:.82rem;color:var(--gray-500);margin-bottom:12px;">{{ qrCible?.codeComplet }}</p>
          <div v-if="chargementQr" class="empty-state">Génération...</div>
          <img v-else-if="qrBase64" :src="'data:image/png;base64,' + qrBase64" alt="QR Code" style="width:240px;height:240px;border:1px solid var(--gray-200);border-radius:8px;" />
          <div style="margin-top:14px;">
            <a v-if="qrBase64" :href="'data:image/png;base64,' + qrBase64" :download="'qr-' + qrCible?.code + '.png'" class="btn btn-primary btn-sm">
              Télécharger
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm suppression -->
    <ConfirmModal
      v-if="confirm.visible"
      titre="Supprimer l'emplacement"
      :message="`Supprimer l'emplacement ${confirm.cible?.codeComplet} ?`"
      type="danger"
      label-confirmer="Supprimer"
      @confirmer="confirmerSuppression"
      @annuler="confirm.visible = false"
    />

    <!-- Toast -->
    <transition name="toast-slide">
      <div v-if="toast.visible" class="toast-error">
        <p class="toast-title">{{ toast.type === 'succes' ? 'Succès' : 'Erreur' }}</p>
        <p class="toast-message">{{ toast.message }}</p>
        <button class="toast-close" @click="toast.visible = false">✕</button>
      </div>
    </transition>
  </div>
</template>

<script>
import { emplacementApi, zoneApi } from '../services/api.js'
import { qrCodeApi } from '../services/api.js'
import ConfirmModal from '../components/ConfirmModal.vue'

export default {
  name: 'EmplacementsPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], zones: [],
      recherche: '', filtreZone: '',
      chargement: true, chargementAction: false, chargementQr: false,
      modal: false, modalQr: false, erreur: '',
      form: { id: null, zoneId: '', rayon: '', etagere: '', code: '', occupe: false },
      qrBase64: null, qrCible: null,
      confirm: { visible: false, cible: null },
      toast: { visible: false, type: 'succes', message: '' }
    }
  },
  computed: {
    listeFiltree() {
      let l = this.liste
      if (this.filtreZone) l = l.filter(e => e.zoneId === this.filtreZone)
      const q = this.recherche.toLowerCase()
      if (!q) return l
      return l.filter(e =>
        e.codeComplet?.toLowerCase().includes(q) ||
        e.zoneNom?.toLowerCase().includes(q) ||
        e.rayon?.toLowerCase().includes(q) ||
        e.etagere?.toLowerCase().includes(q) ||
        e.code?.toLowerCase().includes(q)
      )
    }
  },
  async mounted() {
    await Promise.all([this.charger(), this.chargerZones()])
  },
  methods: {
    async charger() {
      this.chargement = true
      try { const res = await emplacementApi.findAll(); this.liste = res.data }
      finally { this.chargement = false }
    },
    async chargerZones() {
      const res = await zoneApi.findAll()
      this.zones = res.data
    },
    ouvrirModal(e = null) {
      this.erreur = ''
      if (e) {
        this.form = { id: e.id, zoneId: e.zoneId, rayon: e.rayon, etagere: e.etagere, code: e.code, occupe: e.occupe }
      } else {
        this.form = { id: null, zoneId: '', rayon: '', etagere: '', code: '', occupe: false }
      }
      this.modal = true
    },
    async sauvegarder() {
      this.erreur = ''
      if (!this.form.zoneId) { this.erreur = 'La zone est requise.'; return }
      if (!this.form.rayon)   { this.erreur = 'Le rayon est requis.'; return }
      if (!this.form.etagere) { this.erreur = "L'étagère est requise."; return }
      if (!this.form.code)    { this.erreur = 'Le code est requis.'; return }
      this.chargementAction = true
      try {
        if (this.form.id) await emplacementApi.modifier(this.form.id, this.form)
        else await emplacementApi.creer(this.form)
        this.modal = false
        await this.charger()
      } catch (e) {
        this.erreur = e.response?.data?.message || 'Erreur lors de la sauvegarde.'
      } finally { this.chargementAction = false }
    },
    async ouvrirQr(e) {
      this.qrCible = e
      this.qrBase64 = null
      this.chargementQr = true
      this.modalQr = true
      try {
        const res = await qrCodeApi.emplacement(e.id)
        this.qrBase64 = res.data.base64
      } finally { this.chargementQr = false }
    },
    demanderSuppression(e) { this.confirm = { visible: true, cible: e } },
    async confirmerSuppression() {
      try {
        await emplacementApi.supprimer(this.confirm.cible.id)
        this.confirm.visible = false
        await this.charger()
      } catch (e) {
        this.confirm.visible = false
        this.afficherToast(e.response?.data?.message || 'Impossible de supprimer.', 'erreur')
      }
    },
    afficherToast(message, type = 'succes') {
      this.toast = { visible: true, type, message }
      setTimeout(() => { this.toast.visible = false }, 4000)
    }
  }
}
</script>

<style scoped>
.empty-state { text-align:center;padding:32px;color:var(--gray-400); }
.ref-code { font-size:.8rem;background:var(--gray-100);padding:2px 6px;border-radius:4px;font-family:monospace; }
.toast-error { position:fixed;top:24px;right:24px;z-index:9999;background:#fff;border:1px solid #fca5a5;border-left:4px solid var(--danger);border-radius:var(--radius);box-shadow:var(--shadow-lg);padding:14px 16px;max-width:360px;display:flex;flex-direction:column;gap:4px; }
.toast-title { font-weight:700;font-size:.88rem; }
.toast-message { font-size:.83rem;color:var(--gray-700); }
.toast-close { background:none;border:none;cursor:pointer;align-self:flex-end; }
.toast-slide-enter-active,.toast-slide-leave-active { transition:all .25s ease; }
.toast-slide-enter-from,.toast-slide-leave-to { opacity:0;transform:translateX(40px); }
</style>
