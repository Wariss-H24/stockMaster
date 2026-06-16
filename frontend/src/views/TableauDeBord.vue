<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Tableau de bord</h1>
      <p class="page-subtitle">Vue d'ensemble de votre système de gestion des stocks</p>
    </div>

    <!-- KPIs -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background:#e8eef6;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M3 9.5L12 4l9 5.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z" stroke="#1e3a5f" stroke-width="1.8"/>
            <path d="M9 21V12h6v9" stroke="#1e3a5f" stroke-width="1.8"/>
          </svg>
        </div>
        <div class="stat-body">
          <p class="stat-label">Entrepôts</p>
          <p class="stat-value">{{ stats.entrepots }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#dbeafe;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="8" height="10" rx="1.5" stroke="#1d4ed8" stroke-width="1.8"/>
            <rect x="13" y="3" width="8" height="6" rx="1.5" stroke="#1d4ed8" stroke-width="1.8"/>
            <rect x="13" y="13" width="8" height="8" rx="1.5" stroke="#1d4ed8" stroke-width="1.8"/>
            <rect x="3" y="17" width="8" height="4" rx="1.5" stroke="#1d4ed8" stroke-width="1.8"/>
          </svg>
        </div>
        <div class="stat-body">
          <p class="stat-label">Produits</p>
          <p class="stat-value">{{ stats.produits }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#fee2e2;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M12 9v4M12 17h.01" stroke="#dc2626" stroke-width="2" stroke-linecap="round"/>
            <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="#dc2626" stroke-width="1.8"/>
          </svg>
        </div>
        <div class="stat-body">
          <p class="stat-label">Stocks critiques</p>
          <p class="stat-value" style="color:var(--danger);">{{ stats.stocksCritiques }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#dcfce7;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="9" cy="7" r="4" stroke="#16a34a" stroke-width="1.8"/>
            <path d="M3 21v-2a4 4 0 014-4h4a4 4 0 014 4v2" stroke="#16a34a" stroke-width="1.8"/>
          </svg>
        </div>
        <div class="stat-body">
          <p class="stat-label">Utilisateurs</p>
          <p class="stat-value">{{ stats.utilisateurs }}</p>
        </div>
      </div>
    </div>

    <!-- Occupation entrepôts -->
    <div class="card">
      <h2 style="font-size:.95rem;font-weight:700;color:var(--gray-900);margin-bottom:20px;">Taux d'occupation des entrepôts</h2>
      <div v-if="chargement" style="text-align:center;padding:30px;color:var(--gray-400);">Chargement...</div>
      <div v-else-if="entrepots.length === 0" style="text-align:center;padding:30px;color:var(--gray-400);">Aucun entrepôt disponible.</div>
      <div v-else>
        <div v-for="e in entrepots" :key="e.id" style="margin-bottom:18px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:7px;flex-wrap:wrap;gap:6px;">
            <div>
              <span style="font-weight:600;font-size:.875rem;color:var(--gray-900);">{{ e.nom }}</span>
              <span style="font-size:.78rem;color:var(--gray-500);margin-left:8px;">{{ e.adresse }}</span>
            </div>
            <div style="display:flex;align-items:center;gap:10px;">
              <span style="font-size:.8rem;color:var(--gray-500);">{{ e.capaciteUtilisee }} / {{ e.capaciteTotale }}</span>
              <span class="badge" :class="(e.tauxOccupation||0) < 50 ? 'badge-success' : (e.tauxOccupation||0) < 80 ? 'badge-warning' : 'badge-danger'">
                {{ e.tauxOccupation || 0 }}%
              </span>
            </div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill"
              :class="(e.tauxOccupation||0) < 50 ? 'progress-low' : (e.tauxOccupation||0) < 80 ? 'progress-mid' : 'progress-high'"
              :style="{ width: (e.tauxOccupation || 0) + '%' }">
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stocks critiques -->
    <div class="card" v-if="stocksCritiques.length > 0" style="margin-top:20px;">
      <h2 style="font-size:.95rem;font-weight:700;color:var(--danger);margin-bottom:16px;">⚠ Stocks critiques</h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>Produit</th><th>Entrepôt</th><th>Disponible</th><th>Min requis</th></tr>
          </thead>
          <tbody>
            <tr v-for="s in stocksCritiques" :key="s.id" style="background:#fff5f5;">
              <td style="font-weight:600;">{{ s.produitNom }}</td>
              <td>{{ s.entrepotNom }}</td>
              <td style="color:var(--danger);font-weight:700;">{{ s.quantiteDisponible }}</td>
              <td style="color:var(--gray-500);">{{ s.stockMin }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { entrepotApi, produitApi, utilisateurApi, stockApi } from '../services/api.js'

export default {
  name: 'TableauDeBord',
  data() {
    return {
      chargement: true,
      stats: { entrepots: 0, produits: 0, stocksCritiques: 0, utilisateurs: 0 },
      entrepots: [],
      stocksCritiques: []
    }
  },
  async mounted() {
    try {
      const [e, p, u, s] = await Promise.all([
        entrepotApi.findAll(), produitApi.findAll(),
        utilisateurApi.findAll(), stockApi.findAll()
      ])
      this.entrepots = e.data
      this.stocksCritiques = s.data.filter(st => st.stockMin > 0 && st.quantiteDisponible < st.stockMin)
      this.stats = {
        entrepots: e.data.length,
        produits: p.data.length,
        stocksCritiques: this.stocksCritiques.length,
        utilisateurs: u.data.length
      }
    } finally {
      this.chargement = false
    }
  }
}
</script>
