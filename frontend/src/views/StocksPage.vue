<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Stocks</h1>
      <p class="page-subtitle">Suivi des quantités et des emplacements — Module 7</p>
    </div>

    <!-- KPIs rapides -->
    <div class="stats-grid" style="grid-template-columns:repeat(3,1fr);margin-bottom:20px;">
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--navy-xlight);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M4 7h16M4 12h16M4 17h16" stroke="var(--navy)" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="stat-body">
          <p class="stat-label">Références</p>
          <p class="stat-value">{{ liste.length }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--danger-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M12 9v4M12 17h.01" stroke="var(--danger)" stroke-width="2" stroke-linecap="round"/>
            <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="var(--danger)" stroke-width="1.8"/>
          </svg>
        </div>
        <div class="stat-body">
          <p class="stat-label">Stocks critiques</p>
          <p class="stat-value" style="color:var(--danger);">{{ stocksCritiques }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--success-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <polyline points="20 6 9 17 4 12" stroke="var(--success)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-body">
          <p class="stat-label">Stocks OK</p>
          <p class="stat-value" style="color:var(--success);">{{ liste.length - stocksCritiques }}</p>
        </div>
      </div>
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
        <div class="pill-group">
          <button class="pill" :class="{ active: filtreAlerte === false }" @click="filtreAlerte = false">Tous</button>
          <button class="pill" :class="{ active: filtreAlerte === true }" @click="filtreAlerte = true">
            ⚠ Critiques ({{ stocksCritiques }})
          </button>
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Produit</th><th>Entrepôt</th><th>Zone</th>
              <th>Disponible</th><th>Réservé</th><th>Transit</th>
              <th>Stock Min</th><th>État</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="8" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="8" class="empty-state">Aucun stock trouvé.</td>
            </tr>
            <tr v-for="s in listeFiltree" :key="s.id" :class="{ 'row-critical': estCritique(s) }">
              <td>
                <span style="font-weight:600;color:var(--gray-900);">{{ s.produitNom }}</span>
                <div class="text-muted">{{ s.produitReference }}</div>
              </td>
              <td>{{ s.entrepotNom }}</td>
              <td>{{ s.zoneNom || '—' }}</td>
              <td>
                <span :style="estCritique(s) ? 'color:var(--danger);font-weight:700;' : 'font-weight:600;'">
                  {{ s.quantiteDisponible }}
                </span>
              </td>
              <td>
                <span v-if="s.quantiteReservee > 0" class="badge badge-warning">{{ s.quantiteReservee }}</span>
                <span v-else style="color:var(--gray-400);">0</span>
              </td>
              <td style="color:var(--gray-500);">{{ s.quantiteTransit }}</td>
              <td style="font-size:.85rem;">
                <span v-if="s.stockMin > 0" :style="estCritique(s) ? 'color:var(--danger);font-weight:700;' : 'color:var(--gray-600);'">
                  {{ s.stockMin }}
                </span>
                <span v-else style="color:var(--gray-300);">—</span>
              </td>
              <td>
                <span v-if="estCritique(s)" class="badge badge-danger">⚠ Critique</span>
                <span v-else-if="s.stockMin > 0 && s.quantiteDisponible <= s.stockMin * 1.2" class="badge badge-warning">Bas</span>
                <span v-else class="badge badge-success">Normal</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { stockApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'

export default {
  name: 'StocksPage',
  data() {
    return { liste: [], recherche: '', filtreAlerte: false, chargement: true }
  },
  computed: {
    stocksCritiques() {
      return this.liste.filter(s => this.estCritique(s)).length
    },
    listeFiltree() {
      let result = this.liste
      if (this.filtreAlerte) result = result.filter(s => this.estCritique(s))
      const q = this.recherche.toLowerCase()
      if (!q) return result
      return result.filter(s =>
        s.produitNom?.toLowerCase().includes(q) ||
        s.produitReference?.toLowerCase().includes(q) ||
        s.entrepotNom?.toLowerCase().includes(q) ||
        s.zoneNom?.toLowerCase().includes(q)
      )
    }
  },
  async mounted() { await this.charger() },
  methods: {
    estCritique(s) {
      return s.stockMin > 0 && s.quantiteDisponible < s.stockMin
    },
    async charger() {
      this.chargement = true
      try { const res = await stockApi.findAll(); this.liste = res.data } finally { this.chargement = false }
    }
  }
}
</script>

<style scoped>
.empty-state { text-align:center;padding:32px;color:var(--gray-400); }
.text-muted { color: var(--gray-500); font-size: .82rem; margin-top: 2px; }
.row-critical { background: #fff5f5; }
.row-critical:hover { background: #fee2e2 !important; }
.pill-group { display:flex;gap:8px; }
.pill {
  border: 1px solid var(--gray-200);
  border-radius: 999px;
  background: var(--white);
  color: var(--gray-700);
  padding: 6px 14px;
  font-size: .82rem;
  cursor: pointer;
  transition: all .15s;
}
.pill.active { background: var(--navy); color: #fff; border-color: var(--navy); }
.pill:hover:not(.active) { background: var(--gray-50); }
</style>
