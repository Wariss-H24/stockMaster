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
          <p class="stat-label">Zones</p>
          <p class="stat-value">{{ stats.zones }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#fef3c7;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path d="M20 7H4a1 1 0 00-1 1v10a1 1 0 001 1h16a1 1 0 001-1V8a1 1 0 00-1-1z" stroke="#d97706" stroke-width="1.8"/>
            <path d="M16 7V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2" stroke="#d97706" stroke-width="1.8"/>
          </svg>
        </div>
        <div class="stat-body">
          <p class="stat-label">Produits</p>
          <p class="stat-value">{{ stats.produits }}</p>
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
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:7px;">
            <div>
              <span style="font-weight:600;font-size:.875rem;color:var(--gray-900);">{{ e.nom }}</span>
              <span style="font-size:.78rem;color:var(--gray-500);margin-left:8px;">{{ e.adresse }}</span>
            </div>
            <div style="display:flex;align-items:center;gap:10px;">
              <span style="font-size:.8rem;color:var(--gray-500);">{{ e.capaciteUtilisee }} / {{ e.capaciteTotale }}</span>
              <span class="badge" :class="e.tauxOccupation < 50 ? 'badge-success' : e.tauxOccupation < 80 ? 'badge-warning' : 'badge-danger'">
                {{ e.tauxOccupation }}%
              </span>
            </div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill"
              :class="e.tauxOccupation < 50 ? 'progress-low' : e.tauxOccupation < 80 ? 'progress-mid' : 'progress-high'"
              :style="{ width: e.tauxOccupation + '%' }">
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { entrepotApi, zoneApi, produitApi, utilisateurApi } from '../services/api.js'

export default {
  name: 'TableauDeBord',
  data() {
    return {
      chargement: true,
      stats: { entrepots: 0, zones: 0, produits: 0, utilisateurs: 0 },
      entrepots: []
    }
  },
  async mounted() {
    try {
      const [e, z, p, u] = await Promise.all([
        entrepotApi.findAll(), zoneApi.findAll(),
        produitApi.findAll(), utilisateurApi.findAll()
      ])
      this.entrepots = e.data
      this.stats = { entrepots: e.data.length, zones: z.data.length, produits: p.data.length, utilisateurs: u.data.length }
    } finally {
      this.chargement = false
    }
  }
}
</script>
