<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Transferts inter-entrepôts</h1>
      <p class="page-subtitle">Déplacer des produits entre entrepôts — réservé aux ADMIN / GESTIONNAIRE</p>
    </div>

    <div class="card">
      <div class="form-row">
        <div class="form-group">
          <label class="form-label">Produit * </label>
          <select class="form-select" v-model.number="form.produitId">
            <option value="">Sélectionner</option>
            <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.nom }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Quantité * </label>
          <input class="form-input" type="number" min="1" v-model.number="form.quantite" />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="form-label">Entrepôt source * </label>
          <select class="form-select" v-model.number="form.sourceEntrepotId">
            <option value="">Sélectionner</option>
            <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Zone source</label>
          <select class="form-select" v-model.number="form.sourceZoneId" :disabled="!form.sourceEntrepotId">
            <option value="">Aucune</option>
            <option v-for="z in sourceZones" :key="z.id" :value="z.id">{{ z.nom }}{{ z.entrepotNom ? ' — ' + z.entrepotNom : '' }}</option>
          </select>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="form-label">Entrepôt destination * </label>
          <select class="form-select" v-model.number="form.destinationEntrepotId">
            <option value="">Sélectionner</option>
            <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Zone destination</label>
          <select class="form-select" v-model.number="form.destinationZoneId" :disabled="!form.destinationEntrepotId">
            <option value="">Aucune</option>
            <option v-for="z in destinationZones" :key="z.id" :value="z.id">{{ z.nom }}{{ z.entrepotNom ? ' — ' + z.entrepotNom : '' }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">Commentaire</label>
        <textarea class="form-input" v-model="form.commentaire" rows="3" placeholder="Notes de transfert"></textarea>
      </div>

      <div class="form-error" v-if="erreur">{{ erreur }}</div>

      <div class="modal-footer" style="justify-content:flex-start; gap: 16px; margin-top: 16px;">
        <button class="btn btn-outline" @click="reinitialiser">Réinitialiser</button>
        <button class="btn btn-primary" @click="sauvegarder" :disabled="chargement">Lancer le transfert</button>
      </div>
    </div>

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
  </div>
</template>

<script>
import { authStore } from '../services/authStore.js'
import { transfertApi, entrepotApi, zoneApi, produitApi } from '../services/api.js'

export default {
  name: 'TransfertsPage',
  data() {
    return {
      entrepots: [],
      zones: [],
      produits: [],
      form: {
        produitId: null,
        sourceEntrepotId: null,
        sourceZoneId: null,
        destinationEntrepotId: null,
        destinationZoneId: null,
        quantite: 1,
        commentaire: ''
      },
      erreur: '',
      chargement: false,
      toast: { visible: false, message: '', duree: 10000, _timer: null }
    }
  },
  computed: {
    canTransferer() {
      return authStore.aUnRole('ADMIN', 'GESTIONNAIRE')
    },
    sourceZones() {
      return this.zones.filter(z => z.entrepotId === this.form.sourceEntrepotId)
    },
    destinationZones() {
      return this.zones.filter(z => z.entrepotId === this.form.destinationEntrepotId)
    }
  },
  async mounted() {
    await this.chargerDonnees()
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
    async chargerDonnees() {
      const [e, z, p] = await Promise.all([entrepotApi.findAll(), zoneApi.findAll(), produitApi.findAll()])
      this.entrepots = e.data
      this.zones = z.data
      this.produits = p.data
    },
    reinitialiser() {
      this.erreur = ''
      this.form = {
        produitId: null,
        sourceEntrepotId: null,
        sourceZoneId: null,
        destinationEntrepotId: null,
        destinationZoneId: null,
        quantite: 1,
        commentaire: ''
      }
    },
    async sauvegarder() {
      this.erreur = ''
      if (!this.canTransferer) {
        this.afficherToast('Vous n\'êtes pas autorisé à effectuer des transferts.')
        return
      }
      try {
        if (!this.form.produitId) throw new Error('Le produit est requis.')
        if (!this.form.sourceEntrepotId) throw new Error('L\'entrepôt source est requis.')
        if (!this.form.destinationEntrepotId) throw new Error('L\'entrepôt de destination est requis.')
        if (this.form.sourceEntrepotId === this.form.destinationEntrepotId && this.form.sourceZoneId === this.form.destinationZoneId) {
          throw new Error('La source et la destination doivent être différentes.')
        }
        if (!this.form.quantite || this.form.quantite < 1) {
          throw new Error('La quantité doit être au moins de 1.')
        }
        this.chargement = true
        await transfertApi.creer(this.form)
        this.afficherToast('Transfert créé avec succès.', 10000)
        this.reinitialiser()
      } catch (e) {
        this.erreur = e.response?.data?.message || e.message || 'Impossible de créer le transfert.'
        if (!this.erreur) this.erreur = 'Impossible de créer le transfert.'
        this.afficherToast(this.erreur)
      } finally {
        this.chargement = false
      }
    }
  }
}
</script>

<style scoped>
.form-row { display:flex;gap:16px;flex-wrap:wrap;margin-bottom:16px; }
.form-group { flex:1; min-width:240px; }
.form-input, .form-select, textarea { width:100%; }
.form-error { color: var(--danger); margin-top: 12px; font-weight: 600; }
.modal-footer { display:flex; align-items:center; }
.toast-error {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 20;
  display: flex;
  gap: 12px;
  background: #fff5f5;
  border: 1px solid #f5c2c7;
  box-shadow: 0 24px 48px rgba(0,0,0,0.08);
  border-radius: 16px;
  padding: 16px;
  width: min(420px, calc(100vw - 48px));
  align-items: flex-start;
}
.toast-icon { display:flex; align-items:center; justify-content:center; width:36px; height:36px; color:var(--danger); }
.toast-body { flex:1; min-width:0; }
.toast-title { margin:0 0 4px; font-weight:700; }
.toast-message { margin:0; color:var(--gray-700); }
.toast-progress { margin-top: 8px; height: 3px; background: rgba(220, 38, 38, 0.12); border-radius: 999px; overflow: hidden; }
.toast-progress-bar { width: 100%; height: 100%; background: var(--danger); animation: progress-linear linear forwards; }
.toast-close { border:none; background:transparent; color:var(--gray-500); cursor:pointer; }
@keyframes progress-linear { from { transform: translateX(-100%); } to { transform: translateX(0); } }
.toast-slide-enter-active { transition: all .25s ease; }
.toast-slide-leave-active { transition: all .2s ease; }
.toast-slide-enter-from { opacity: 0; transform: translateX(40px); }
.toast-slide-leave-to { opacity: 0; transform: translateX(40px); }
</style>
