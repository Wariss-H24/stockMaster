<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Mouvements de stock</h1>
      <p class="page-subtitle">Journal des entrées et sorties — Module 7</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="pill-group">
          <button class="pill" :class="{ active: filtre === 'TOUS' }" @click="changerFiltre('TOUS')">Tous</button>
          <button class="pill" :class="{ active: filtre === 'ENTREE' }" @click="changerFiltre('ENTREE')">Entrées</button>
          <button class="pill" :class="{ active: filtre === 'SORTIE' }" @click="changerFiltre('SORTIE')">Sorties</button>
          <button class="pill" :class="{ active: filtre === 'TRANSFERT' }" @click="changerFiltre('TRANSFERT')">Transferts</button>
        </div>
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Filtrer par produit, entrepôt, zone..." />
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Date</th><th>Produit</th><th>Entrepôt</th><th>Zone</th><th>Type</th><th>Quantité</th><th>Source</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="7" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="7" class="empty-state">Aucun mouvement trouvé.</td>
            </tr>
            <tr v-for="m in listeFiltree" :key="m.id">
              <td>{{ formaterDate(m.date) }}</td>
              <td>{{ m.produitNom }}</td>
              <td>{{ m.entrepotNom }}</td>
              <td>{{ m.zoneNom || '—' }}</td>
              <td>
                <span class="badge" :class="m.type === 'ENTREE' ? 'badge-success' : m.type === 'SORTIE' ? 'badge-danger' : 'badge-warning'">
                  {{ m.type }}</span>
              </td>
              <td>{{ m.quantite }}</td>
              <td>{{ m.source || 'Système' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { mouvementStockApi } from '../services/api.js'

export default {
  name: 'MouvementsStockPage',
  data() {
    return { liste: [], recherche: '', filtre: 'TOUS', chargement: true }
  },
  computed: {
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      return this.liste.filter(m =>
        (!q || m.produitNom?.toLowerCase().includes(q) || m.entrepotNom?.toLowerCase().includes(q) || m.zoneNom?.toLowerCase().includes(q) || m.source?.toLowerCase().includes(q))
      )
    }
  },
  async mounted() { await this.charger() },
  methods: {
    async charger() {
      this.chargement = true
      try { const res = await mouvementStockApi.findAll(); this.liste = res.data } finally { this.chargement = false }
    },
    async changerFiltre(type) {
      this.filtre = type
      this.chargement = true
      try {
        if (type === 'TOUS') {
          const res = await mouvementStockApi.findAll(); this.liste = res.data
        } else {
          const res = await mouvementStockApi.findByType(type); this.liste = res.data
        }
      } finally { this.chargement = false }
    },
    formaterDate(date) {
      return new Date(date).toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    }
  }
}
</script>

<style scoped>
.empty-state { text-align:center;padding:32px;color:var(--gray-400); }
.pill-group { display:flex;gap:10px;flex-wrap:wrap; }
.pill {
  border: 1px solid var(--gray-200);
  border-radius: 999px;
  background: var(--white);
  color: var(--gray-700);
  padding: 8px 14px;
  cursor: pointer;
  transition: all .15s;
}
.pill.active { background: var(--navy); color: #fff; border-color: var(--navy); }
.pill:hover { background: var(--gray-50); }
</style>
