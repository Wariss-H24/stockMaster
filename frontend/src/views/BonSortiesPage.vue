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
        <button class="btn btn-primary" @click="ouvrirModal()">
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
                <button class="btn btn-primary btn-sm" @click="valider(b)" v-if="b.statut === 'BROUILLON'">Valider</button>
                <button class="btn btn-danger btn-sm" @click="demanderSuppression(b)" v-if="b.statut === 'BROUILLON'">Supprimer</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-overlay" v-if="deleteModal" @click.self="annulerSuppression()">
      <div class="modal small-modal">
        <div class="modal-header">
          <h3 class="modal-title">Confirmer la suppression</h3>
        </div>
        <div class="modal-body">
          <p>Voulez-vous vraiment supprimer ce bon de sortie en brouillon ?</p>
          <p v-if="toDelete"><strong>Id:</strong> {{ toDelete.id }} — <strong>Destination:</strong> {{ toDelete.destination }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="annulerSuppression">Annuler</button>
          <button class="btn btn-danger" @click="confirmerSuppression">Supprimer</button>
        </div>
      </div>
    </div>

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

export default {
  name: 'BonSortiesPage',
  data() {
    return {
      liste: [], entrepots: [], produits: [], recherche: '', chargement: true,
      modal: false, erreur: '',
      form: { id: null, entrepotId: '', destination: '', commentaire: '', lignes: [] },
      deleteModal: false,
      toDelete: null
    }
  },
  computed: {
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
  methods: {
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
        this.form = { ...b, lignes: b.lignes.map(l => ({ ...l })) }
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
        if (this.form.id) await sortieApi.modifier(this.form.id, this.form)
        else await sortieApi.creer(this.form)
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
        alert(e.response?.data?.message || 'Impossible de valider le bon.')
      }
    },
    demanderSuppression(bon) {
      this.toDelete = bon
      this.deleteModal = true
    },
    annulerSuppression() {
      this.toDelete = null
      this.deleteModal = false
    },
    async confirmerSuppression() {
      try {
        await sortieApi.supprimer(this.toDelete.id)
        this.annulerSuppression()
        await this.charger()
      } catch (e) {
        this.annulerSuppression()
        alert(e.response?.data?.message || 'Impossible de supprimer le bon.')
      }
    },
    formaterDate(date) {
      return new Date(date).toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
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
</style>
