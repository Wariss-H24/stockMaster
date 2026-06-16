<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Bons de réception</h1>
      <p class="page-subtitle">Traçabilité des arrivées de marchandises — Module 8</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher par fournisseur, entrepôt, statut..." />
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
              <th>Fournisseur</th><th>Entrepôt</th><th>Zone</th><th>Statut</th><th>Date</th><th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="6" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="6" class="empty-state">Aucun bon de réception trouvé.</td>
            </tr>
            <tr v-for="b in listeFiltree" :key="b.id">
              <td>{{ b.fournisseurNom || b.fournisseurId }}</td>
              <td>{{ b.entrepotNom || b.entrepotId }}</td>
              <td>{{ b.zoneNom || '—' }}</td>
              <td>
                <span class="badge" :class="b.statut === 'VALIDE' ? 'badge-success' : 'badge-warning'">{{ b.statut }}</span>
              </td>
              <td>{{ formaterDate(b.date) }}</td>
              <td style="text-align:right;">
                <button class="btn btn-outline btn-sm" @click="ouvrirModal(b)">Voir</button>
                <button class="btn btn-primary btn-sm" @click="valider(b)" v-if="b.statut === 'BROUILLON'">Valider</button>
                <button class="btn btn-danger btn-sm" @click="supprimer(b)" v-if="b.statut === 'BROUILLON'">Supprimer</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal large-modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ form.id ? 'Modifier le bon de réception' : 'Nouveau bon de réception' }}</h3>
          <button class="modal-close" @click="modal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Fournisseur *</label>
              <select class="form-select" v-model.number="form.fournisseurId">
                <option value="">Sélectionner</option>
                <option v-for="f in fournisseurs" :key="f.id" :value="f.id">{{ f.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Entrepôt *</label>
              <select class="form-select" v-model.number="form.entrepotId">
                <option value="">Sélectionner</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Zone</label>
              <select class="form-select" v-model.number="form.zoneId">
                <option value="">Aucune</option>
                <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.nom }} — {{ z.entrepotNom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Contrôle qualité</label>
              <label class="form-check">
                <input type="checkbox" v-model="form.controleQualiteOk" />
                <span>Contrôle validé</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Commentaire</label>
            <textarea class="form-input" v-model="form.commentaire" rows="3" placeholder="Notes d'arrivée"></textarea>
          </div>
          <div class="line-table">
            <div class="line-header">Lignes de réception</div>
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
                <div class="form-group">
                  <label class="form-label">Prix achat</label>
                  <input class="form-input" type="number" min="0" step="0.01" v-model.number="ligne.prixAchat" />
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
import { receptionApi, fournisseurApi, entrepotApi, zoneApi, produitApi } from '../services/api.js'

export default {
  name: 'BonReceptionsPage',
  data() {
    return {
      liste: [], fournisseurs: [], entrepots: [], zones: [], produits: [],
      recherche: '', chargement: true, modal: false, erreur: '',
      form: { id: null, fournisseurId: '', entrepotId: '', zoneId: null, commentaire: '', controleQualiteOk: false, lignes: [] }
    }
  },
  computed: {
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(b =>
        b.fournisseurNom?.toLowerCase().includes(q) ||
        b.entrepotNom?.toLowerCase().includes(q) ||
        b.zoneNom?.toLowerCase().includes(q) ||
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
      try { const res = await receptionApi.findAll(); this.liste = res.data } finally { this.chargement = false }
    },
    async chargerDonnees() {
      const [f, e, z, p] = await Promise.all([
        fournisseurApi.findAll(), entrepotApi.findAll(), zoneApi.findAll(), produitApi.findAll()
      ])
      this.fournisseurs = f.data
      this.entrepots = e.data
      this.zones = z.data
      this.produits = p.data
    },
    ouvrirModal(b = null) {
      this.erreur = ''
      if (b) {
        this.form = {
          id: b.id,
          fournisseurId: b.fournisseurId,
          entrepotId: b.entrepotId,
          zoneId: b.zoneId,
          commentaire: b.commentaire,
          controleQualiteOk: b.controleQualiteOk,
          lignes: b.lignes.map(l => ({ ...l }))
        }
      } else {
        this.form = { id: null, fournisseurId: '', entrepotId: '', zoneId: null, commentaire: '', controleQualiteOk: false, lignes: [] }
      }
      this.modal = true
    },
    ajouterLigne() {
      this.form.lignes.push({ produitId: '', quantite: 1, prixAchat: 0 })
    },
    supprimerLigne(index) {
      this.form.lignes.splice(index, 1)
    },
    async sauvegarder() {
      this.erreur = ''
      try {
        if (!this.form.fournisseurId) throw new Error('Le fournisseur est requis.')
        if (!this.form.entrepotId) throw new Error('L’entrepôt est requis.')
        if (this.form.lignes.length === 0) throw new Error('Ajoutez au moins une ligne de réception.')
        if (this.form.lignes.some(l => !l.produitId || l.quantite < 1)) {
          throw new Error('Chaque ligne doit contenir un produit et une quantité valide.')
        }
        if (this.form.id) await receptionApi.modifier(this.form.id, this.form)
        else await receptionApi.creer(this.form)
        this.modal = false
        await this.charger()
      } catch (e) {
        this.erreur = e.response?.data?.message || e.message || 'Impossible de sauvegarder le bon.'
      }
    },
    async valider(bon) {
      try {
        await receptionApi.valider(bon.id)
        await this.charger()
      } catch (e) {
        alert(e.response?.data?.message || 'Impossible de valider le bon.')
      }
    },
    async supprimer(bon) {
      if (!confirm('Supprimer ce bon de réception en brouillon ?')) return
      try {
        await receptionApi.supprimer(bon.id)
        await this.charger()
      } catch (e) {
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
