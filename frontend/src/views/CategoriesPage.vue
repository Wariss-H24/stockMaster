<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Catégories</h1>
      <p class="page-subtitle">Gestion des familles de produits — Module 5</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher une catégorie..." />
        </div>
        <button class="btn btn-primary" @click="ouvrirModal()" v-if="peutEcrire">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouvelle catégorie
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nom</th><th>Description</th><th>Statut</th><th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="4" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="4" class="empty-state">Aucune catégorie trouvée.</td>
            </tr>
            <tr v-for="c in listeFiltree" :key="c.id">
              <td><strong>{{ c.nom }}</strong></td>
              <td>{{ c.description || '—' }}</td>
              <td>
                <span class="badge" :class="c.actif ? 'badge-success' : 'badge-danger'">
                  {{ c.actif ? 'Active' : 'Désactivée' }}
                </span>
              </td>
              <td style="text-align:right;">
                <button class="btn btn-outline btn-sm" @click="ouvrirModal(c)">Éditer</button>
                <button class="btn btn-danger btn-sm" @click="demanderDesactivation(c)" v-if="c.actif">Désactiver</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ form.id ? 'Modifier la catégorie' : 'Nouvelle catégorie' }}</h3>
          <button class="modal-close" @click="modal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Nom *</label>
            <input class="form-input" v-model="form.nom" placeholder="Ex: Électronique" />
          </div>
          <div class="form-group">
            <label class="form-label">Description</label>
            <textarea class="form-input" v-model="form.description" rows="3" placeholder="Description courte"></textarea>
          </div>
          <label class="form-check">
            <input type="checkbox" v-model="form.actif" />
            <span>Catégorie active</span>
          </label>
          <div class="form-error" v-if="erreur">{{ erreur }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modal = false">Annuler</button>
          <button class="btn btn-primary" @click="sauvegarder">Sauvegarder</button>
        </div>
      </div>
    </div>

    <ConfirmModal
      v-if="confirm.visible"
      titre="Désactiver la catégorie"
      :message="`Voulez-vous désactiver « ${confirm.cible?.nom} » ? Cette catégorie sera masquée.`"
      type="warning"
      label-confirmer="Désactiver"
      @confirmer="confirmerDesactivation"
      @annuler="confirm.visible = false"
    />
  </div>
</template>

<script>
import { categoryApi } from '../services/api.js'
import ConfirmModal from '../components/ConfirmModal.vue'
import { authStore } from '../services/authStore.js'

export default {
  name: 'CategoriesPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], recherche: '', chargement: true,
      modal: false, erreur: '', confirm: { visible: false, cible: null },
      form: { id: null, nom: '', description: '', actif: true }
    }
  },
  computed: {
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(c => c.nom?.toLowerCase().includes(q) || c.description?.toLowerCase().includes(q))
    },
    peutEcrire() {
      return authStore.aUnRole('ADMIN', 'GESTIONNAIRE')
    }
  },
  async mounted() { await this.charger() },
  methods: {
    async charger() {
      this.chargement = true
      try { const res = await categoryApi.findAll(); this.liste = res.data } finally { this.chargement = false }
    },
    ouvrirModal(c = null) {
      this.form = c ? { ...c } : { id: null, nom: '', description: '', actif: true }
      this.erreur = ''
      this.modal = true
    },
    async sauvegarder() {
      this.erreur = ''
      try {
        if (!this.form.nom?.trim()) throw new Error('Le nom est requis.')
        if (this.form.id) await categoryApi.modifier(this.form.id, this.form)
        else await categoryApi.creer(this.form)
        this.modal = false
        await this.charger()
      } catch (e) {
        this.erreur = e.response?.data?.message || e.message || 'Impossible de sauvegarder.'
      }
    },
    demanderDesactivation(c) {
      this.confirm = { visible: true, cible: c }
    },
    async confirmerDesactivation() {
      await categoryApi.desactiver(this.confirm.cible.id)
      this.confirm = { visible: false, cible: null }
      await this.charger()
    }
  }
}
</script>

<style scoped>
.empty-state { text-align:center;padding:32px;color:var(--gray-400); }
textarea.form-input { min-height: 110px; resize: vertical; }
</style>
