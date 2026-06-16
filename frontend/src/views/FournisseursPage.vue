<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Fournisseurs</h1>
      <p class="page-subtitle">Gestion des partenaires d'achat — Module 6</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher un fournisseur..." />
        </div>
        <button class="btn btn-primary" @click="ouvrirModal()" v-if="peutEcrire">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouveau fournisseur
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nom</th><th>Téléphone</th><th>Email</th><th>Contact</th><th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="5" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="5" class="empty-state">Aucun fournisseur trouvé.</td>
            </tr>
            <tr v-for="f in listeFiltree" :key="f.id">
              <td><strong>{{ f.nom }}</strong></td>
              <td>{{ f.telephone || '—' }}</td>
              <td>{{ f.email || '—' }}</td>
              <td>{{ f.contactPrincipal || '—' }}</td>
              <td style="text-align:right;">
                <button class="btn btn-outline btn-sm" @click="voirLivraisons(f)">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none"><path d="M4 6h16M4 10h16M4 14h8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
                  Livraisons
                </button>
                <button class="btn btn-outline btn-sm" @click="ouvrirModal(f)">Éditer</button>
                <button class="btn btn-danger btn-sm" @click="demanderDesactivation(f)" v-if="f.actif">Désactiver</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ form.id ? 'Modifier le fournisseur' : 'Nouveau fournisseur' }}</h3>
          <button class="modal-close" @click="modal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Nom *</label>
              <input class="form-input" v-model="form.nom" placeholder="Ex: Fournitures Pro" />
            </div>
            <div class="form-group">
              <label class="form-label">Téléphone</label>
              <input class="form-input" v-model="form.telephone" placeholder="01 23 45 67 89" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Email</label>
              <input class="form-input" v-model="form.email" placeholder="contact@fournisseur.fr" />
            </div>
            <div class="form-group">
              <label class="form-label">Contact principal</label>
              <input class="form-input" v-model="form.contactPrincipal" placeholder="Marie Durand" />
            </div>
          </div>
          <label class="form-check">
            <input type="checkbox" v-model="form.actif" />
            <span>Fournisseur actif</span>
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
      titre="Désactiver le fournisseur"
      :message="`Voulez-vous désactiver « ${confirm.cible?.nom} » ? Il sera masqué des listes actives.`"
      type="warning"
      label-confirmer="Désactiver"
      @confirmer="confirmerDesactivation"
      @annuler="confirm.visible = false"
    />

    <!-- Panneau historique livraisons -->
    <div class="modal-overlay" v-if="historiqueModal" @click.self="historiqueModal = false">
      <div class="modal" style="max-width:700px;">
        <div class="modal-header">
          <h3 class="modal-title">Livraisons — {{ fournisseurCourant?.nom }}</h3>
          <button class="modal-close" @click="historiqueModal = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <div class="modal-body">
          <div v-if="chargementLivraisons" style="text-align:center;padding:24px;color:var(--gray-400);">Chargement...</div>
          <div v-else-if="livraisons.length === 0" style="text-align:center;padding:24px;color:var(--gray-400);">Aucune livraison enregistrée.</div>
          <div class="table-wrap" v-else>
            <table>
              <thead>
                <tr><th>#</th><th>Entrepôt</th><th>Zone</th><th>Statut</th><th>Date</th><th>Lignes</th></tr>
              </thead>
              <tbody>
                <tr v-for="b in livraisons" :key="b.id">
                  <td><code style="font-size:.78rem;">{{ b.id }}</code></td>
                  <td>{{ b.entrepotNom }}</td>
                  <td>{{ b.zoneNom || '—' }}</td>
                  <td><span class="badge" :class="b.statut === 'VALIDE' ? 'badge-success' : 'badge-warning'">{{ b.statut }}</span></td>
                  <td>{{ formaterDate(b.date) }}</td>
                  <td>{{ b.lignes?.length ?? 0 }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="historiqueModal = false">Fermer</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { fournisseurApi } from '../services/api.js'
import ConfirmModal from '../components/ConfirmModal.vue'
import { authStore } from '../services/authStore.js'

export default {
  name: 'FournisseursPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], recherche: '', chargement: true,
      modal: false, erreur: '', confirm: { visible: false, cible: null },
      form: { id: null, nom: '', telephone: '', email: '', contactPrincipal: '', actif: true },
      historiqueModal: false, fournisseurCourant: null, livraisons: [], chargementLivraisons: false
    }
  },
  computed: {
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(f =>
        f.nom?.toLowerCase().includes(q) ||
        f.telephone?.toLowerCase().includes(q) ||
        f.email?.toLowerCase().includes(q) ||
        f.contactPrincipal?.toLowerCase().includes(q)
      )
    },
    peutEcrire() {
      return authStore.aUnRole('ADMIN', 'GESTIONNAIRE')
    }
  },
  async mounted() { await this.charger() },
  methods: {
    async charger() {
      this.chargement = true
      try { const res = await fournisseurApi.findAll(); this.liste = res.data } finally { this.chargement = false }
    },
    ouvrirModal(f = null) {
      this.form = f ? { ...f } : { id: null, nom: '', telephone: '', email: '', contactPrincipal: '', actif: true }
      this.erreur = ''
      this.modal = true
    },
    async sauvegarder() {
      this.erreur = ''
      try {
        if (!this.form.nom?.trim()) throw new Error('Le nom est requis.')
        if (this.form.id) await fournisseurApi.modifier(this.form.id, this.form)
        else await fournisseurApi.creer(this.form)
        this.modal = false
        await this.charger()
      } catch (e) {
        this.erreur = e.response?.data?.message || e.message || 'Impossible de sauvegarder.'
      }
    },
    demanderDesactivation(f) {
      this.confirm = { visible: true, cible: f }
    },
    async confirmerDesactivation() {
      await fournisseurApi.desactiver(this.confirm.cible.id)
      this.confirm = { visible: false, cible: null }
      await this.charger()
    },
    async voirLivraisons(f) {
      this.fournisseurCourant = f
      this.livraisons = []
      this.historiqueModal = true
      this.chargementLivraisons = true
      try { const res = await fournisseurApi.livraisons(f.id); this.livraisons = res.data }
      finally { this.chargementLivraisons = false }
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
</style>
