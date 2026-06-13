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
            <div class="form-group">
              <label class="form-label">Capacité utilisée</label>
              <input class="form-input" type="number" v-model.number="form.capaciteUtilisee" min="0" />
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
      form: { id: null, nom: '', entrepotId: '', capaciteTotale: 0, capaciteUtilisee: 0 }
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
  methods: {
    ouvrirModal(z = null) {
      this.erreur = ''
      this.form = z
        ? { id: z.id, nom: z.nom, entrepotId: z.entrepotId, capaciteTotale: z.capaciteTotale, capaciteUtilisee: z.capaciteUtilisee }
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
        this.erreur = e.response?.data?.message || 'Erreur lors de la sauvegarde.'
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
