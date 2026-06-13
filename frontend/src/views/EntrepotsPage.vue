<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Entrepôts</h1>
      <p class="page-subtitle">Gestion des entrepôts — Module 2</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher un entrepôt..." />
        </div>
        <!-- Bouton visible uniquement pour ADMIN et GESTIONNAIRE -->
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirModal()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouvel entrepôt
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nom</th><th>Adresse</th><th>Responsable</th>
              <th>Occupation</th><th>Statut</th>
              <th v-if="peutEcrire || peutSupprimer" style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="6" style="text-align:center;padding:32px;color:var(--gray-400);">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="6" style="text-align:center;padding:32px;color:var(--gray-400);">Aucun entrepôt trouvé.</td>
            </tr>
            <tr v-for="e in listeFiltree" :key="e.id">
              <td><span style="font-weight:600;color:var(--gray-900);">{{ e.nom }}</span></td>
              <td style="color:var(--gray-500);font-size:.82rem;">{{ e.adresse }}</td>
              <td>{{ e.responsable || '—' }}</td>
              <td style="min-width:160px;">
                <div style="display:flex;align-items:center;gap:8px;">
                  <div class="progress-bar">
                    <div class="progress-fill"
                      :class="e.tauxOccupation < 50 ? 'progress-low' : e.tauxOccupation < 80 ? 'progress-mid' : 'progress-high'"
                      :style="{ width: e.tauxOccupation + '%' }">
                    </div>
                  </div>
                  <span style="font-size:.78rem;color:var(--gray-500);min-width:32px;">{{ e.tauxOccupation }}%</span>
                </div>
              </td>
              <td>
                <span class="badge" :class="e.actif ? 'badge-success' : 'badge-danger'">
                  {{ e.actif ? 'Actif' : 'Inactif' }}
                </span>
              </td>
              <td v-if="peutEcrire || peutSupprimer" style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <!-- Éditer : ADMIN + GESTIONNAIRE -->
                  <button v-if="peutEcrire" class="btn btn-outline btn-sm" @click="ouvrirModal(e)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="1.8"/>
                    </svg>
                    Éditer
                  </button>
                  <!-- Désactiver : ADMIN seulement -->
                  <button v-if="peutSupprimer && e.actif" class="btn btn-danger btn-sm" @click="demanderDesactivation(e)">
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
          <h3 class="modal-title">{{ form.id ? 'Modifier l\'entrepôt' : 'Nouvel entrepôt' }}</h3>
          <button class="modal-close" @click="modal = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Nom *</label>
            <input class="form-input" v-model="form.nom" placeholder="ex: ENT-Paris" />
          </div>
          <div class="form-group">
            <label class="form-label">Adresse *</label>
            <input class="form-input" v-model="form.adresse" placeholder="12 Rue de la Logistique, Paris" />
          </div>
          <div class="form-group">
            <label class="form-label">Responsable</label>
            <input class="form-input" v-model="form.responsable" placeholder="Jean Dupont" />
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
          <label class="form-check">
            <input type="checkbox" v-model="form.actif" />
            <span>Entrepôt actif</span>
          </label>
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
      titre="Désactiver l'entrepôt"
      :message="`Voulez-vous désactiver « ${confirm.cible?.nom} » ? Il ne sera plus visible dans les listes actives.`"
      type="warning"
      label-confirmer="Désactiver"
      @confirmer="confirmerDesactivation"
      @annuler="confirm.visible = false"
    />
  </div>
</template>

<script>
import { entrepotApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'
import ConfirmModal from '../components/ConfirmModal.vue'

export default {
  name: 'EntrepotsPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], chargement: true, modal: false, erreur: '', recherche: '',
      confirm: { visible: false, cible: null },
      form: { id: null, nom: '', adresse: '', responsable: '', capaciteTotale: 0, capaciteUtilisee: 0, actif: true }
    }
  },
  computed: {
    // ADMIN + GESTIONNAIRE peuvent créer/modifier
    peutEcrire()    { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    // Seul ADMIN peut désactiver
    peutSupprimer() { return authStore.aRole('ADMIN') },
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(e =>
        e.nom?.toLowerCase().includes(q) ||
        e.adresse?.toLowerCase().includes(q) ||
        e.responsable?.toLowerCase().includes(q)
      )
    }
  },
  async mounted() { await this.charger() },
  methods: {
    async charger() {
      this.chargement = true
      try { const r = await entrepotApi.findAll(); this.liste = r.data }
      finally { this.chargement = false }
    },
    ouvrirModal(e = null) {
      this.erreur = ''
      this.form = e ? { ...e } : { id: null, nom: '', adresse: '', responsable: '', capaciteTotale: 0, capaciteUtilisee: 0, actif: true }
      this.modal = true
    },
    async sauvegarder() {
      this.erreur = ''
      try {
        if (this.form.id) await entrepotApi.modifier(this.form.id, this.form)
        else await entrepotApi.creer(this.form)
        this.modal = false
        this.charger()
      } catch (e) {
        this.erreur = e.response?.data?.message || 'Erreur lors de la sauvegarde.'
      }
    },
    // Ouvre la modale de confirmation
    demanderDesactivation(e) {
      this.confirm = { visible: true, cible: e }
    },
    async confirmerDesactivation() {
      await entrepotApi.desactiver(this.confirm.cible.id)
      this.confirm = { visible: false, cible: null }
      this.charger()
    }
  }
}
</script>
