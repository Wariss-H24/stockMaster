<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Stocks</h1>
      <p class="page-subtitle">Suivi des quantités et des emplacements — Module 7</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher un produit, zone ou entrepôt..." />
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Produit</th><th>Entrepôt</th><th>Zone</th><th>Disponible</th><th>Min</th><th>Max</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="6" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="6" class="empty-state">Aucun stock trouvé.</td>
            </tr>
            <tr v-for="s in listeFiltree" :key="s.id">
              <td>
                <strong>{{ s.produitNom }}</strong>
                <div class="text-muted">{{ s.produitReference }}</div>
              </td>
              <td>{{ s.entrepotNom }}</td>
              <td>{{ s.zoneNom || '—' }}</td>
              <td><strong>{{ s.quantiteDisponible }}</strong></td>
              <td>{{ s.stockMin }}</td>
              <td>{{ s.stockMax }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { stockApi } from '../services/api.js'

export default {
  name: 'StocksPage',
  data() {
    return { liste: [], recherche: '', chargement: true }
  },
  computed: {
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(s =>
        s.produitNom?.toLowerCase().includes(q) ||
        s.produitReference?.toLowerCase().includes(q) ||
        s.entrepotNom?.toLowerCase().includes(q) ||
        s.zoneNom?.toLowerCase().includes(q)
      )
    }
  },
  async mounted() { await this.charger() },
  methods: {
    async charger() {
      this.chargement = true
      try { const res = await stockApi.findAll(); this.liste = res.data } finally { this.chargement = false }
    }
  }
}
</script>

<style scoped>
.empty-state { text-align:center;padding:32px;color:var(--gray-400); }
.text-muted { color: var(--gray-500); font-size: .82rem; margin-top: 4px; }
</style>
