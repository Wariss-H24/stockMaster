<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Reporting & Exports</h1>
      <p class="page-subtitle">Module 14 — Rapports PDF, Excel, CSV</p>
    </div>

    <!-- Section 1 : Stocks -->
    <div class="card report-card" style="margin-bottom:20px;">
      <div class="report-card-header">
        <div class="report-card-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.8"/><path d="M8 12h8M8 8h8M8 16h5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
        </div>
        <div>
          <h3 class="report-card-title">Rapport de Stock</h3>
          <p class="report-card-desc">État actuel de tous les stocks par produit et entrepôt</p>
        </div>
      </div>
      <div class="report-actions">
        <button class="btn btn-navy" @click="telecharger('pdfStock')" :disabled="chargements.pdfStock">
          <span v-if="chargements.pdfStock" class="spinner"></span>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6z" stroke="currentColor" stroke-width="1.8"/><path d="M14 2v6h6M12 18v-6M9 15l3 3 3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
          Télécharger PDF
        </button>
        <button class="btn btn-success" @click="telecharger('excelStock')" :disabled="chargements.excelStock">
          <span v-if="chargements.excelStock" class="spinner"></span>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.8"/><path d="M8 8l8 8M16 8l-8 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          Télécharger Excel
        </button>
        <button class="btn btn-outline" @click="telecharger('csvStock')" :disabled="chargements.csvStock">
          <span v-if="chargements.csvStock" class="spinner"></span>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6z" stroke="currentColor" stroke-width="1.8"/><path d="M14 2v6h6M9 13h6M9 17h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          Télécharger CSV
        </button>
      </div>
    </div>

    <!-- Section 2 : Mouvements -->
    <div class="card report-card" style="margin-bottom:20px;">
      <div class="report-card-header">
        <div class="report-card-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M4 12h7M13 6l5 6-5 6M21 7v10" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div>
          <h3 class="report-card-title">Rapport des Mouvements</h3>
          <p class="report-card-desc">Historique complet des entrées, sorties et transferts</p>
        </div>
      </div>
      <div class="report-actions">
        <button class="btn btn-navy" @click="telecharger('pdfMouvements')" :disabled="chargements.pdfMouvements">
          <span v-if="chargements.pdfMouvements" class="spinner"></span>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6z" stroke="currentColor" stroke-width="1.8"/><path d="M14 2v6h6M12 18v-6M9 15l3 3 3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
          PDF Mouvements
        </button>
        <button class="btn btn-success" @click="telecharger('excelMouvements')" :disabled="chargements.excelMouvements">
          <span v-if="chargements.excelMouvements" class="spinner"></span>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.8"/><path d="M8 8l8 8M16 8l-8 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          Excel Mouvements
        </button>
        <button class="btn btn-outline" @click="telecharger('csvMouvements')" :disabled="chargements.csvMouvements">
          <span v-if="chargements.csvMouvements" class="spinner"></span>
          <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6z" stroke="currentColor" stroke-width="1.8"/><path d="M14 2v6h6M9 13h6M9 17h4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          CSV Mouvements
        </button>
      </div>
    </div>

    <!-- Toast -->
    <transition name="toast-slide">
      <div v-if="toast.visible" :class="['toast-notif', toast.type === 'succes' ? 'toast-succes' : 'toast-erreur']">
        <div class="toast-icon">
          <svg v-if="toast.type === 'succes'" width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
        </div>
        <div class="toast-body">
          <p class="toast-title">{{ toast.type === 'succes' ? 'Succès' : 'Erreur' }}</p>
          <p class="toast-message">{{ toast.message }}</p>
          <div class="toast-progress"><div class="toast-progress-bar" :style="{ animationDuration: toast.duree + 'ms' }"></div></div>
        </div>
        <button class="toast-close" @click="fermerToast">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        </button>
      </div>
    </transition>
  </div>
</template>

<script>
import { reportingApi } from '../services/api.js'

export default {
  name: 'ReportingPage',
  data() {
    return {
      chargements: {
        pdfStock: false, excelStock: false, csvStock: false,
        pdfMouvements: false, excelMouvements: false, csvMouvements: false
      },
      toast: { visible: false, type: 'succes', message: '', duree: 5000, _timer: null }
    }
  },
  beforeUnmount() { clearTimeout(this.toast._timer) },
  methods: {
    async telecharger(type) {
      this.chargements[type] = true
      const nomsFichiers = {
        pdfStock: 'rapport-stock.pdf',
        excelStock: 'rapport-stock.xlsx',
        csvStock: 'rapport-stock.csv',
        pdfMouvements: 'rapport-mouvements.pdf',
        excelMouvements: 'rapport-mouvements.xlsx',
        csvMouvements: 'rapport-mouvements.csv'
      }
      try {
        const res = await reportingApi[type]()
        const blob = new Blob([res.data], { type: res.headers['content-type'] })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = nomsFichiers[type]
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
        this.afficherToast(`Fichier téléchargé : ${nomsFichiers[type]}`, 'succes')
      } catch {
        this.afficherToast('Téléchargement échoué. Veuillez réessayer.', 'erreur')
      } finally {
        this.chargements[type] = false
      }
    },
    afficherToast(message, type = 'succes', duree = 5000) {
      clearTimeout(this.toast._timer)
      this.toast = { visible: true, type, message, duree, _timer: null }
      this.toast._timer = setTimeout(() => { this.toast.visible = false }, duree)
    },
    fermerToast() { clearTimeout(this.toast._timer); this.toast.visible = false }
  }
}
</script>

<style scoped>
.report-card-header {
  display: flex; align-items: flex-start; gap: 16px;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--gray-100);
  background: linear-gradient(to right, var(--navy-xlight, #eef2ff), #ffffff);
  border-radius: var(--radius) var(--radius) 0 0;
}
.report-card-icon {
  width: 44px; height: 44px; border-radius: 10px;
  background: var(--navy-xlight, #eef2ff); color: var(--navy);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.report-card-title { font-size: 1rem; font-weight: 700; color: var(--gray-900); margin-bottom: 4px; }
.report-card-desc  { font-size: .85rem; color: var(--gray-500); }
.report-actions {
  display: flex; flex-wrap: wrap; gap: 12px;
  padding: 20px 24px;
}
.btn-navy { background: var(--navy); color: #fff; border: none; }
.btn-navy:hover { opacity: .9; }
.btn-navy:disabled { opacity: .55; cursor: not-allowed; }
.btn-success { background: var(--success); color: #fff; border: none; }
.btn-success:hover { opacity: .9; }
.btn-success:disabled { opacity: .55; cursor: not-allowed; }
.btn:disabled { opacity: .55; cursor: not-allowed; }
.spinner {
  display: inline-block; width: 13px; height: 13px;
  border: 2px solid rgba(255,255,255,.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
/* Toast */
.toast-notif { position:fixed;top:24px;right:24px;z-index:9999;display:flex;align-items:flex-start;gap:12px;border-radius:var(--radius);box-shadow:var(--shadow-lg);padding:16px 14px 12px 16px;max-width:420px;min-width:300px; }
.toast-succes { background:#f0fdf4;border:1px solid #86efac;border-left:4px solid var(--success); }
.toast-succes .toast-icon,.toast-succes .toast-title { color:var(--success); }
.toast-succes .toast-progress-bar { background:var(--success); }
.toast-succes .toast-progress { background:rgba(22,163,74,.12); }
.toast-erreur { background:#fff5f5;border:1px solid #fca5a5;border-left:4px solid var(--danger); }
.toast-erreur .toast-icon,.toast-erreur .toast-title { color:var(--danger); }
.toast-erreur .toast-progress-bar { background:var(--danger); }
.toast-erreur .toast-progress { background:rgba(220,38,38,.12); }
.toast-icon { flex-shrink:0;margin-top:1px; }
.toast-body { flex:1;min-width:0; }
.toast-title { font-weight:700;font-size:.88rem;margin-bottom:3px; }
.toast-message { font-size:.83rem;color:var(--gray-700);line-height:1.4; }
.toast-progress { margin-top:8px;height:3px;border-radius:999px;overflow:hidden; }
.toast-progress-bar { width:100%;height:100%;border-radius:999px;animation:toast-drain linear forwards; }
.toast-close { background:none;border:none;cursor:pointer;color:var(--gray-400);padding:2px;flex-shrink:0;border-radius:4px;display:flex;align-items:center; }
.toast-close:hover { color:var(--gray-700);background:var(--gray-100); }
@keyframes toast-drain { from { width:100%; } to { width:0%; } }
.toast-slide-enter-active { transition:all .25s ease; }
.toast-slide-leave-active { transition:all .2s ease; }
.toast-slide-enter-from,.toast-slide-leave-to { opacity:0;transform:translateX(40px); }
</style>
