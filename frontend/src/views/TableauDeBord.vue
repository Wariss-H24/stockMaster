<template>
  <div class="dashboard">

    <!-- En-tête avec date -->
    <div class="page-header" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px;">
      <div>
        <h1 class="page-title">Tableau de bord</h1>
        <p class="page-subtitle">Vue en temps réel — {{ dateActuelle }}</p>
      </div>
      <button class="btn btn-outline btn-sm" @click="charger" :disabled="chargement">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path d="M4 4v5h.58M20 20v-5h-.58M4.58 9A8 8 0 0120 13.42M19.42 15A8 8 0 014 10.58"
                stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        Actualiser
      </button>
    </div>

    <!-- Skeleton loader -->
    <div v-if="chargement" class="skeleton-grid">
      <div v-for="i in 8" :key="i" class="skeleton-card"></div>
    </div>

    <!-- KPIs principaux -->
    <div v-else class="kpi-grid">
      <div class="kpi-card kpi-navy">
        <div class="kpi-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M3 9.5L12 4l9 5.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z" stroke="currentColor" stroke-width="2"/><path d="M9 21V12h6v9" stroke="currentColor" stroke-width="2"/></svg></div>
        <div>
          <p class="kpi-label">Entrepôts actifs</p>
          <p class="kpi-value">{{ kpis.nbEntrepots }}</p>
        </div>
      </div>

      <div class="kpi-card kpi-blue">
        <div class="kpi-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M20 7H4a1 1 0 00-1 1v10a1 1 0 001 1h16a1 1 0 001-1V8a1 1 0 00-1-1z" stroke="currentColor" stroke-width="2"/><path d="M16 7V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2" stroke="currentColor" stroke-width="2"/></svg></div>
        <div>
          <p class="kpi-label">Produits référencés</p>
          <p class="kpi-value">{{ kpis.nbProduits }}</p>
        </div>
      </div>

      <div class="kpi-card kpi-danger">
        <div class="kpi-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="currentColor" stroke-width="2"/><path d="M12 9v4M12 17h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg></div>
        <div>
          <p class="kpi-label">Stocks critiques</p>
          <p class="kpi-value">{{ kpis.nbStocksCritiques }}</p>
          <p class="kpi-sub" v-if="kpis.nbStocksCritiques > 0">⚠ Action requise</p>
        </div>
      </div>

      <div class="kpi-card kpi-success">
        <div class="kpi-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
        <div>
          <p class="kpi-label">Stocks OK</p>
          <p class="kpi-value">{{ kpis.nbStocksOk }}</p>
        </div>
      </div>

      <div class="kpi-card kpi-teal">
        <div class="kpi-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M5 12h14M12 5l7 7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
        <div>
          <p class="kpi-label">Entrées ce mois</p>
          <p class="kpi-value">{{ kpis.entreesDuMois }}</p>
        </div>
      </div>

      <div class="kpi-card kpi-orange">
        <div class="kpi-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
        <div>
          <p class="kpi-label">Sorties ce mois</p>
          <p class="kpi-value">{{ kpis.sortiesDuMois }}</p>
        </div>
      </div>

      <div class="kpi-card kpi-purple">
        <div class="kpi-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M4 12h7M13 6l5 6-5 6M21 7v10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
        <div>
          <p class="kpi-label">Transferts en transit</p>
          <p class="kpi-value">{{ kpis.nbTransfertsEnCours }}</p>
        </div>
      </div>

      <div class="kpi-card kpi-gold">
        <div class="kpi-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4zM3 6h18M16 10a4 4 0 01-8 0" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
        <div>
          <p class="kpi-label">Commandes en attente</p>
          <p class="kpi-value">{{ kpis.nbCommandesEnAttente }}</p>
        </div>
      </div>
    </div>

    <!-- Valeur totale du stock -->
    <div v-if="!chargement" class="valeur-banner">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-4H9v-2h2V9h2v2h2v2h-2v4z" fill="currentColor" opacity=".7"/></svg>
      <span>Valeur totale du stock :</span>
      <strong>{{ formaterMontant(kpis.valeurTotaleStock) }}</strong>
    </div>

    <!-- Grille principale : graphiques + tableaux -->
    <div v-if="!chargement" class="dashboard-grid">

      <!-- Évolution mensuelle -->
      <div class="card chart-card">
        <h3 class="chart-title">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          Évolution des mouvements — 6 derniers mois
        </h3>
        <div class="bar-chart" v-if="kpis.evolutionMensuelle && kpis.evolutionMensuelle.length">
          <div class="bar-chart-inner">
            <div v-for="mois in kpis.evolutionMensuelle" :key="mois.mois" class="bar-group">
              <div class="bars">
                <div class="bar bar-entree" :style="{ height: barHeight(mois.entrees, maxMouvement) + 'px' }" :title="mois.entrees + ' entrées'">
                  <span class="bar-val">{{ mois.entrees }}</span>
                </div>
                <div class="bar bar-sortie" :style="{ height: barHeight(mois.sorties, maxMouvement) + 'px' }" :title="mois.sorties + ' sorties'">
                  <span class="bar-val">{{ mois.sorties }}</span>
                </div>
              </div>
              <div class="bar-label">{{ mois.mois }}</div>
            </div>
          </div>
          <div class="chart-legend">
            <span class="legend-item"><span class="legend-dot" style="background:#2563eb;"></span>Entrées</span>
            <span class="legend-item"><span class="legend-dot" style="background:#f97316;"></span>Sorties</span>
          </div>
        </div>
        <p v-else class="empty-chart">Aucun mouvement enregistré.</p>
      </div>

      <!-- Répartition par entrepôt (donut SVG) -->
      <div class="card chart-card">
        <h3 class="chart-title">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 2a10 10 0 0110 10" stroke="#2563eb" stroke-width="2.5"/></svg>
          Répartition des stocks par entrepôt
        </h3>
        <div v-if="kpis.repartitionEntrepots && kpis.repartitionEntrepots.length" class="donut-wrapper">
          <svg width="160" height="160" viewBox="0 0 160 160">
            <circle cx="80" cy="80" r="60" fill="none" stroke="#e2e8f0" stroke-width="22"/>
            <circle v-for="(seg, i) in donutSegments" :key="i"
              cx="80" cy="80" r="60" fill="none"
              :stroke="donutColors[i % donutColors.length]"
              stroke-width="22"
              :stroke-dasharray="seg.dash"
              :stroke-dashoffset="seg.offset"
              style="transition:stroke-dasharray .5s ease;"
            />
          </svg>
          <div class="donut-legend">
            <div v-for="(e, i) in kpis.repartitionEntrepots" :key="i" class="donut-legend-item">
              <span class="legend-dot" :style="{ background: donutColors[i % donutColors.length] }"></span>
              <span>{{ e.nom }}</span>
              <strong>{{ e.quantite }}</strong>
            </div>
          </div>
        </div>
        <p v-else class="empty-chart">Aucune donnée de stock.</p>
      </div>

      <!-- Top produits -->
      <div class="card chart-card">
        <h3 class="chart-title">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M18 20V10M12 20V4M6 20v-6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          Top 5 produits les plus mouvementés
        </h3>
        <div v-if="kpis.topProduits && kpis.topProduits.length" class="top-produits">
          <div v-for="(p, i) in kpis.topProduits" :key="i" class="top-row">
            <div class="top-rank" :class="'rank-' + (i+1)">{{ i + 1 }}</div>
            <div class="top-name">{{ p.nom }}</div>
            <div class="top-bar-wrap">
              <div class="top-bar" :style="{ width: ((p.mouvements / kpis.topProduits[0].mouvements) * 100) + '%', background: donutColors[i % donutColors.length] }"></div>
            </div>
            <div class="top-count">{{ p.mouvements }}</div>
          </div>
        </div>
        <p v-else class="empty-chart">Aucun mouvement enregistré.</p>
      </div>

      <!-- Alertes actives -->
      <div class="card chart-card">
        <h3 class="chart-title" style="color:var(--danger)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 01-3.46 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          Alertes stock actives
        </h3>
        <div v-if="kpis.alertesActives && kpis.alertesActives.length">
          <div v-for="a in kpis.alertesActives.slice(0,6)" :key="a.stockId" class="alerte-row">
            <div class="alerte-niveau" :class="a.niveau === 'CRITIQUE' ? 'niveau-critique' : 'niveau-faible'">
              {{ a.niveau }}
            </div>
            <div class="alerte-info">
              <p class="alerte-produit">{{ a.produitNom }}</p>
              <p class="alerte-detail">{{ a.entrepotNom }} — dispo : <strong>{{ a.quantiteDisponible }}</strong> / min : {{ a.stockMin }}</p>
            </div>
            <div class="alerte-barre">
              <div class="alerte-barre-fill"
                :style="{ width: Math.min(100, (a.quantiteDisponible / Math.max(1, a.stockMin)) * 100) + '%',
                          background: a.niveau === 'CRITIQUE' ? 'var(--danger)' : 'var(--warning)' }">
              </div>
            </div>
          </div>
          <router-link to="/alertes" class="voir-plus">Voir toutes les alertes →</router-link>
        </div>
        <div v-else class="no-alertes">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" style="opacity:.25;display:block;margin:0 auto 8px;"><path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          <p>Tous les stocks sont OK</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { dashboardApi } from '../services/api.js'

export default {
  name: 'TableauDeBord',
  data() {
    return {
      chargement: true,
      kpis: {
        nbEntrepots: 0, nbProduits: 0, nbStocksCritiques: 0, nbStocksOk: 0,
        entreesDuMois: 0, sortiesDuMois: 0, nbTransfertsEnCours: 0, nbCommandesEnAttente: 0,
        valeurTotaleStock: 0, topProduits: [], evolutionMensuelle: [],
        repartitionEntrepots: [], alertesActives: []
      },
      donutColors: ['#2563eb', '#16a34a', '#f97316', '#8b5cf6', '#ec4899', '#06b6d4']
    }
  },
  computed: {
    dateActuelle() {
      return new Date().toLocaleDateString('fr-FR', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
      })
    },
    maxMouvement() {
      if (!this.kpis.evolutionMensuelle?.length) return 1
      return Math.max(1, ...this.kpis.evolutionMensuelle.flatMap(m => [m.entrees, m.sorties]))
    },
    donutSegments() {
      const data = this.kpis.repartitionEntrepots || []
      const total = data.reduce((s, e) => s + (e.quantite || 0), 0) || 1
      const circonference = 2 * Math.PI * 60
      let offset = 0
      return data.map(e => {
        const pct = (e.quantite || 0) / total
        const dash = `${pct * circonference} ${(1 - pct) * circonference}`
        const seg = { dash, offset: -offset * circonference - circonference / 4 }
        offset += pct
        return seg
      })
    }
  },
  async mounted() {
    await this.charger()
  },
  methods: {
    async charger() {
      this.chargement = true
      try {
        const res = await dashboardApi.kpis()
        this.kpis = res.data
      } catch (e) {
        console.error('Dashboard load error:', e)
      } finally {
        this.chargement = false
      }
    },
    barHeight(val, max) {
      return Math.max(4, Math.round((val / max) * 120))
    },
    formaterMontant(v) {
      return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(v || 0)
    }
  }
}
</script>

<style scoped>
.dashboard { padding-bottom: 32px; }

/* Skeleton */
.skeleton-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 16px; margin-bottom: 24px; }
.skeleton-card { height: 100px; background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius); }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* KPI Grid */
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }

.kpi-card {
  border-radius: var(--radius);
  padding: 20px 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow);
  transition: transform .15s, box-shadow .15s;
  cursor: default;
}
.kpi-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }

.kpi-icon {
  width: 48px; height: 48px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  background: rgba(255,255,255,.25);
}
.kpi-label { font-size: .72rem; font-weight: 600; text-transform: uppercase; letter-spacing: .06em; opacity: .8; margin-bottom: 3px; }
.kpi-value { font-size: 1.9rem; font-weight: 800; line-height: 1; }
.kpi-sub   { font-size: .72rem; margin-top: 3px; opacity: .85; font-weight: 600; }

.kpi-navy   { background: var(--navy);   color: #fff; }
.kpi-blue   { background: #2563eb;       color: #fff; }
.kpi-danger { background: var(--danger); color: #fff; }
.kpi-success{ background: var(--success);color: #fff; }
.kpi-teal   { background: #0891b2;       color: #fff; }
.kpi-orange { background: #ea580c;       color: #fff; }
.kpi-purple { background: #7c3aed;       color: #fff; }
.kpi-gold   { background: #d97706;       color: #fff; }

/* Valeur totale */
.valeur-banner {
  display: flex; align-items: center; gap: 10px;
  background: linear-gradient(135deg, #1e3a5f, #2563eb);
  color: #fff;
  border-radius: var(--radius);
  padding: 14px 24px;
  margin-bottom: 20px;
  font-size: .95rem;
}
.valeur-banner strong { font-size: 1.25rem; font-weight: 800; margin-left: 4px; }

/* Dashboard grid */
.dashboard-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

/* Graphiques */
.chart-card { padding: 20px 24px; }
.chart-title {
  display: flex; align-items: center; gap: 8px;
  font-size: .9rem; font-weight: 700; color: var(--gray-800);
  margin-bottom: 20px;
}

/* Bar chart */
.bar-chart { overflow-x: auto; }
.bar-chart-inner {
  display: flex; align-items: flex-end; gap: 16px;
  height: 150px; padding-bottom: 28px; min-width: 300px;
}
.bar-group { display: flex; flex-direction: column; align-items: center; flex: 1; }
.bars { display: flex; align-items: flex-end; gap: 4px; height: 120px; }
.bar {
  width: 22px; border-radius: 4px 4px 0 0;
  display: flex; align-items: flex-start; justify-content: center;
  transition: height .4s cubic-bezier(.34,1.56,.64,1);
  position: relative;
}
.bar-entree { background: #2563eb; }
.bar-sortie { background: #f97316; }
.bar-val {
  font-size: .6rem; font-weight: 700; color: #fff;
  position: absolute; top: 3px; white-space: nowrap;
}
.bar-label { font-size: .7rem; color: var(--gray-500); margin-top: 6px; text-align: center; white-space: nowrap; }
.chart-legend { display: flex; gap: 16px; margin-top: 8px; }
.legend-item { display: flex; align-items: center; gap: 5px; font-size: .78rem; color: var(--gray-600); }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.empty-chart { text-align: center; padding: 40px; color: var(--gray-400); font-size: .85rem; }

/* Donut */
.donut-wrapper { display: flex; align-items: center; gap: 24px; flex-wrap: wrap; }
.donut-legend { flex: 1; min-width: 140px; }
.donut-legend-item { display: flex; align-items: center; gap: 8px; padding: 5px 0; font-size: .84rem; color: var(--gray-700); border-bottom: 1px solid var(--gray-100); }
.donut-legend-item:last-child { border-bottom: none; }
.donut-legend-item span:nth-child(2) { flex: 1; }
.donut-legend-item strong { color: var(--navy); font-weight: 700; }

/* Top produits */
.top-produits { display: flex; flex-direction: column; gap: 10px; }
.top-row { display: flex; align-items: center; gap: 10px; }
.top-rank {
  width: 24px; height: 24px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: .72rem; font-weight: 800; flex-shrink: 0; color: #fff;
}
.rank-1 { background: #f59e0b; }
.rank-2 { background: #94a3b8; }
.rank-3 { background: #b45309; }
.rank-4, .rank-5 { background: var(--gray-400); }
.top-name { font-size: .84rem; color: var(--gray-700); width: 130px; flex-shrink: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.top-bar-wrap { flex: 1; height: 8px; background: var(--gray-100); border-radius: 999px; overflow: hidden; }
.top-bar { height: 100%; border-radius: 999px; transition: width .5s ease; }
.top-count { font-size: .84rem; font-weight: 700; color: var(--navy); width: 32px; text-align: right; flex-shrink: 0; }

/* Alertes */
.alerte-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid var(--gray-100); }
.alerte-row:last-of-type { border-bottom: none; }
.alerte-niveau {
  font-size: .65rem; font-weight: 700; padding: 3px 8px;
  border-radius: 999px; flex-shrink: 0; text-transform: uppercase; letter-spacing: .04em;
}
.niveau-critique { background: var(--danger-bg); color: var(--danger); }
.niveau-faible   { background: var(--warning-bg); color: var(--warning); }
.alerte-info { flex: 1; min-width: 0; }
.alerte-produit { font-size: .84rem; font-weight: 600; color: var(--gray-800); margin-bottom: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.alerte-detail  { font-size: .75rem; color: var(--gray-500); }
.alerte-barre   { width: 60px; height: 5px; background: var(--gray-200); border-radius: 999px; overflow: hidden; flex-shrink: 0; }
.alerte-barre-fill { height: 100%; border-radius: 999px; transition: width .4s; }
.voir-plus { display: block; text-align: center; margin-top: 12px; font-size: .82rem; color: var(--blue); text-decoration: none; font-weight: 600; }
.voir-plus:hover { text-decoration: underline; }
.no-alertes { text-align: center; padding: 30px 20px; color: var(--gray-400); font-size: .85rem; }

/* Responsive */
@media (max-width: 1200px) {
  .kpi-grid { grid-template-columns: repeat(4, 1fr); }
  .dashboard-grid { grid-template-columns: 1fr; }
}
@media (max-width: 900px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .skeleton-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .kpi-grid { grid-template-columns: 1fr 1fr; }
  .kpi-value { font-size: 1.5rem; }
}
</style>
