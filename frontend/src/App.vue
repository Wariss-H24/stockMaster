<template>
  <div class="layout" :class="{ 'sidebar-open': sidebarOuverte }">

    <!-- Overlay mobile (clic pour fermer) -->
    <div class="sidebar-overlay" @click="sidebarOuverte = false"></div>

    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="brand">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <rect width="28" height="28" rx="6" fill="#1e3a5f"/>
            <path d="M6 10h16M6 14h10M6 18h13" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span class="brand-name">StockMaster</span>
        </div>
        <!-- Bouton fermer (mobile) -->
        <button class="sidebar-close" @click="sidebarOuverte = false">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M5 5l10 10M15 5L5 15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <nav class="sidebar-nav">
        <p class="nav-section-label">Principal</p>
        <router-link to="/tableau-de-bord" class="nav-item" @click="sidebarOuverte = false">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="8" height="8" rx="2" stroke="currentColor" stroke-width="1.8"/>
            <rect x="13" y="3" width="8" height="8" rx="2" stroke="currentColor" stroke-width="1.8"/>
            <rect x="3" y="13" width="8" height="8" rx="2" stroke="currentColor" stroke-width="1.8"/>
            <rect x="13" y="13" width="8" height="8" rx="2" stroke="currentColor" stroke-width="1.8"/>
          </svg>
          <span>Tableau de bord</span>
        </router-link>

        <p class="nav-section-label">Gestion</p>
        <router-link to="/entrepots" class="nav-item" @click="sidebarOuverte = false">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M3 9.5L12 4l9 5.5V20a1 1 0 01-1 1H4a1 1 0 01-1-1V9.5z" stroke="currentColor" stroke-width="1.8"/>
            <path d="M9 21V12h6v9" stroke="currentColor" stroke-width="1.8"/>
          </svg>
          <span>Entrepôts</span>
        </router-link>
        <router-link to="/zones" class="nav-item" @click="sidebarOuverte = false">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="3" y="3" width="8" height="10" rx="1.5" stroke="currentColor" stroke-width="1.8"/>
            <rect x="13" y="3" width="8" height="6" rx="1.5" stroke="currentColor" stroke-width="1.8"/>
            <rect x="13" y="13" width="8" height="8" rx="1.5" stroke="currentColor" stroke-width="1.8"/>
            <rect x="3" y="17" width="8" height="4" rx="1.5" stroke="currentColor" stroke-width="1.8"/>
          </svg>
          <span>Zones</span>
        </router-link>
        <router-link to="/produits" class="nav-item" @click="sidebarOuverte = false">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M20 7H4a1 1 0 00-1 1v10a1 1 0 001 1h16a1 1 0 001-1V8a1 1 0 00-1-1z" stroke="currentColor" stroke-width="1.8"/>
            <path d="M16 7V5a2 2 0 00-2-2h-4a2 2 0 00-2 2v2" stroke="currentColor" stroke-width="1.8"/>
            <line x1="12" y1="12" x2="12" y2="12.01" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
          <span>Produits</span>
        </router-link>

        <p class="nav-section-label">Administration</p>
        <router-link to="/utilisateurs" class="nav-item" @click="sidebarOuverte = false">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>
            <path d="M3 21v-2a4 4 0 014-4h4a4 4 0 014 4v2" stroke="currentColor" stroke-width="1.8"/>
            <path d="M16 3.13a4 4 0 010 7.75M21 21v-2a4 4 0 00-3-3.85" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <span>Utilisateurs</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">A</div>
          <div>
            <p class="user-name">Administrateur</p>
            <p class="user-role">ADMIN</p>
          </div>
        </div>
      </div>
    </aside>

    <!-- Contenu principal -->
    <div class="main-wrapper">
      <!-- Topbar -->
      <header class="topbar">
        <button class="burger-btn" @click="sidebarOuverte = !sidebarOuverte">
          <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
            <path d="M3 6h16M3 11h16M3 16h16" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
        </button>
        <div class="topbar-title">{{ titreRoute }}</div>
        <div class="topbar-right">
          <span class="topbar-badge">v1.0</span>
        </div>
      </header>

      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return { sidebarOuverte: false }
  },
  computed: {
    titreRoute() {
      const titres = {
        '/tableau-de-bord': 'Tableau de bord',
        '/entrepots': 'Entrepôts',
        '/zones': 'Zones de stockage',
        '/produits': 'Produits',
        '/utilisateurs': 'Utilisateurs'
      }
      return titres[this.$route.path] || 'StockMaster'
    }
  }
}
</script>

<style>
/* =============================================
   RESET & VARIABLES
   ============================================= */
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

:root {
  /* Palette Enterprise */
  --navy:        #1e3a5f;
  --navy-light:  #2a4f80;
  --navy-xlight: #e8eef6;
  --blue:        #2563eb;
  --blue-light:  #dbeafe;
  --blue-dark:   #1d4ed8;

  /* Neutres */
  --white:       #ffffff;
  --gray-50:     #f8fafc;
  --gray-100:    #f1f5f9;
  --gray-200:    #e2e8f0;
  --gray-300:    #cbd5e1;
  --gray-400:    #94a3b8;
  --gray-500:    #64748b;
  --gray-700:    #334155;
  --gray-900:    #0f172a;

  /* Sémantiques */
  --success:     #16a34a;
  --success-bg:  #dcfce7;
  --warning:     #d97706;
  --warning-bg:  #fef3c7;
  --danger:      #dc2626;
  --danger-bg:   #fee2e2;
  --info-bg:     #dbeafe;
  --info:        #1d4ed8;

  /* Layout */
  --sidebar-w:   256px;
  --topbar-h:    60px;
  --radius-sm:   6px;
  --radius:      10px;
  --radius-lg:   14px;
  --shadow-sm:   0 1px 2px rgba(0,0,0,.05);
  --shadow:      0 1px 3px rgba(0,0,0,.08), 0 1px 2px rgba(0,0,0,.04);
  --shadow-md:   0 4px 8px rgba(0,0,0,.08), 0 2px 4px rgba(0,0,0,.04);
  --shadow-lg:   0 10px 24px rgba(0,0,0,.1), 0 4px 8px rgba(0,0,0,.06);
}

html { font-size: 15px; }
body {
  font-family: 'Inter', 'Segoe UI', system-ui, sans-serif;
  background: var(--gray-100);
  color: var(--gray-900);
  line-height: 1.5;
}

/* =============================================
   LAYOUT
   ============================================= */
.layout { display: flex; min-height: 100vh; }

/* =============================================
   SIDEBAR
   ============================================= */
.sidebar {
  width: var(--sidebar-w);
  min-height: 100vh;
  background: var(--navy);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0; left: 0; bottom: 0;
  z-index: 300;
  transition: transform .25s ease;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: var(--topbar-h);
  border-bottom: 1px solid rgba(255,255,255,.08);
  flex-shrink: 0;
}

.brand { display: flex; align-items: center; gap: 10px; }
.brand-name {
  font-size: 1rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: .3px;
}

.sidebar-close {
  display: none;
  background: none;
  border: none;
  color: rgba(255,255,255,.6);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
}
.sidebar-close:hover { color: #fff; background: rgba(255,255,255,.1); }

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.nav-section-label {
  font-size: .68rem;
  font-weight: 700;
  letter-spacing: .1em;
  text-transform: uppercase;
  color: rgba(255,255,255,.35);
  padding: 14px 8px 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--radius-sm);
  color: rgba(255,255,255,.65);
  text-decoration: none;
  font-size: .88rem;
  font-weight: 500;
  transition: background .15s, color .15s;
  margin-bottom: 2px;
}
.nav-item:hover { background: rgba(255,255,255,.08); color: #fff; }
.nav-item.router-link-active {
  background: rgba(37,99,235,.4);
  color: #fff;
  font-weight: 600;
}
.nav-item svg { flex-shrink: 0; opacity: .85; }
.nav-item.router-link-active svg { opacity: 1; }

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,.08);
  flex-shrink: 0;
}
.user-info { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 34px; height: 34px;
  border-radius: 50%;
  background: var(--blue);
  color: #fff;
  font-weight: 700;
  font-size: .9rem;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.user-name { font-size: .82rem; font-weight: 600; color: #fff; }
.user-role { font-size: .72rem; color: rgba(255,255,255,.45); margin-top: 1px; }

/* Overlay mobile */
.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.45);
  z-index: 299;
}

/* =============================================
   MAIN WRAPPER
   ============================================= */
.main-wrapper {
  margin-left: var(--sidebar-w);
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* =============================================
   TOPBAR
   ============================================= */
.topbar {
  height: var(--topbar-h);
  background: var(--white);
  border-bottom: 1px solid var(--gray-200);
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 16px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.burger-btn {
  display: none;
  background: none;
  border: none;
  color: var(--gray-700);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  transition: background .15s;
}
.burger-btn:hover { background: var(--gray-100); }

.topbar-title {
  font-size: .95rem;
  font-weight: 600;
  color: var(--gray-900);
  flex: 1;
}

.topbar-badge {
  font-size: .72rem;
  font-weight: 600;
  color: var(--gray-500);
  background: var(--gray-100);
  border: 1px solid var(--gray-200);
  padding: 3px 8px;
  border-radius: 999px;
}

/* =============================================
   MAIN CONTENT
   ============================================= */
.main-content { padding: 28px 28px 40px; flex: 1; }

/* =============================================
   PAGE HEADER
   ============================================= */
.page-header { margin-bottom: 24px; }
.page-title { font-size: 1.35rem; font-weight: 700; color: var(--gray-900); }
.page-subtitle { font-size: .85rem; color: var(--gray-500); margin-top: 3px; }

/* =============================================
   CARDS
   ============================================= */
.card {
  background: var(--white);
  border-radius: var(--radius);
  border: 1px solid var(--gray-200);
  box-shadow: var(--shadow);
  padding: 24px;
}

/* =============================================
   STATS GRID
   ============================================= */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--white);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 20px 22px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
}
.stat-icon {
  width: 42px; height: 42px;
  border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-body {}
.stat-label {
  font-size: .75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .06em;
  color: var(--gray-500);
}
.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--gray-900);
  line-height: 1.2;
  margin-top: 3px;
}

/* =============================================
   TABLE
   ============================================= */
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: .875rem; }
thead th {
  background: var(--gray-50);
  color: var(--gray-500);
  font-weight: 600;
  font-size: .72rem;
  text-transform: uppercase;
  letter-spacing: .07em;
  padding: 10px 16px;
  text-align: left;
  border-bottom: 1px solid var(--gray-200);
  white-space: nowrap;
}
tbody tr { transition: background .1s; }
tbody tr:not(:last-child) { border-bottom: 1px solid var(--gray-100); }
tbody tr:hover { background: var(--gray-50); }
tbody td { padding: 13px 16px; color: var(--gray-700); vertical-align: middle; }

/* =============================================
   BADGES
   ============================================= */
.badge {
  display: inline-flex; align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: .72rem;
  font-weight: 600;
  letter-spacing: .02em;
  white-space: nowrap;
}
.badge-success { background: var(--success-bg); color: var(--success); }
.badge-danger  { background: var(--danger-bg);  color: var(--danger); }
.badge-warning { background: var(--warning-bg); color: var(--warning); }
.badge-info    { background: var(--info-bg);    color: var(--info); }
.badge-navy    { background: var(--navy-xlight); color: var(--navy); }

/* =============================================
   BOUTONS
   ============================================= */
.btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  cursor: pointer;
  font-size: .85rem;
  font-weight: 600;
  line-height: 1;
  transition: all .15s;
  white-space: nowrap;
}
.btn:active { transform: translateY(1px); }

.btn-primary {
  background: var(--navy);
  color: #fff;
  border-color: var(--navy);
}
.btn-primary:hover { background: var(--navy-light); border-color: var(--navy-light); }

.btn-outline {
  background: var(--white);
  color: var(--gray-700);
  border-color: var(--gray-300);
}
.btn-outline:hover { background: var(--gray-50); border-color: var(--gray-400); }

.btn-danger {
  background: var(--white);
  color: var(--danger);
  border-color: var(--gray-200);
}
.btn-danger:hover { background: var(--danger-bg); border-color: var(--danger); }

.btn-sm { padding: 5px 10px; font-size: .78rem; }

/* =============================================
   FORMULAIRE
   ============================================= */
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { margin-bottom: 14px; }
.form-label {
  display: block;
  font-size: .78rem;
  font-weight: 600;
  color: var(--gray-700);
  margin-bottom: 5px;
}
.form-input, .form-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--gray-300);
  border-radius: var(--radius-sm);
  font-size: .875rem;
  color: var(--gray-900);
  background: var(--white);
  transition: border-color .15s, box-shadow .15s;
  outline: none;
}
.form-input:focus, .form-select:focus {
  border-color: var(--blue);
  box-shadow: 0 0 0 3px rgba(37,99,235,.1);
}
.form-input::placeholder { color: var(--gray-400); }
.form-check { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.form-check input[type="checkbox"] { width: 15px; height: 15px; cursor: pointer; accent-color: var(--blue); }
.form-check span { font-size: .875rem; color: var(--gray-700); font-weight: 500; }

/* =============================================
   MODAL
   ============================================= */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15,23,42,.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 400;
  padding: 16px;
}
.modal {
  background: var(--white);
  border-radius: var(--radius-lg);
  width: 100%; max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
  animation: modal-in .18s ease;
}
@keyframes modal-in {
  from { opacity: 0; transform: translateY(-12px) scale(.98); }
  to   { opacity: 1; transform: none; }
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 24px 0;
}
.modal-title { font-size: 1.05rem; font-weight: 700; color: var(--gray-900); }
.modal-close {
  background: none; border: none; cursor: pointer;
  color: var(--gray-400); padding: 4px; border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
}
.modal-close:hover { background: var(--gray-100); color: var(--gray-700); }
.modal-body { padding: 20px 24px; }
.modal-footer {
  display: flex; gap: 10px; justify-content: flex-end;
  padding: 16px 24px 20px;
  border-top: 1px solid var(--gray-100);
}

/* =============================================
   BARRE DE PROGRESSION
   ============================================= */
.progress-bar {
  background: var(--gray-200);
  border-radius: 999px;
  height: 5px;
  overflow: hidden;
  flex: 1;
}
.progress-fill { height: 100%; border-radius: 999px; transition: width .4s ease; }
.progress-low  { background: var(--success); }
.progress-mid  { background: var(--warning); }
.progress-high { background: var(--danger); }

/* =============================================
   TOOLBAR (search + actions)
   ============================================= */
.toolbar {
  display: flex; align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
  flex-wrap: wrap;
}
.search-box {
  display: flex; align-items: center; gap: 8px;
  border: 1px solid var(--gray-300);
  border-radius: var(--radius-sm);
  padding: 7px 12px;
  background: var(--white);
  transition: border-color .15s, box-shadow .15s;
}
.search-box:focus-within {
  border-color: var(--blue);
  box-shadow: 0 0 0 3px rgba(37,99,235,.1);
}
.search-box svg { color: var(--gray-400); flex-shrink: 0; }
.search-box input {
  border: none; outline: none;
  font-size: .875rem; color: var(--gray-900);
  background: transparent;
  width: 200px;
}
.search-box input::placeholder { color: var(--gray-400); }

/* =============================================
   MESSAGE ERREUR
   ============================================= */
.form-error {
  font-size: .8rem;
  color: var(--danger);
  background: var(--danger-bg);
  border: 1px solid #fca5a5;
  border-radius: var(--radius-sm);
  padding: 8px 12px;
  margin-top: 4px;
}

/* =============================================
   RESPONSIVE — Tablet (< 1024px)
   ============================================= */
@media (max-width: 1023px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}

/* =============================================
   RESPONSIVE — Mobile (< 768px)
   ============================================= */
@media (max-width: 767px) {
  /* Sidebar cachée par défaut, glisse depuis la gauche */
  .sidebar { transform: translateX(-100%); }
  .sidebar-close { display: flex; }

  /* Quand ouverte */
  .layout.sidebar-open .sidebar { transform: translateX(0); }
  .layout.sidebar-open .sidebar-overlay { display: block; }

  /* Main wrapper sans marge left */
  .main-wrapper { margin-left: 0; }

  /* Burger visible */
  .burger-btn { display: flex; }

  /* Content padding réduit */
  .main-content { padding: 16px 14px 32px; }

  .stats-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; }
  .stat-card { padding: 14px 14px; }
  .stat-value { font-size: 1.4rem; }

  .form-row { grid-template-columns: 1fr; }

  .toolbar { flex-direction: column; align-items: stretch; }
  .search-box input { width: 100%; }
  .search-box { flex: 1; }
}

@media (max-width: 480px) {
  .stats-grid { grid-template-columns: 1fr 1fr; }
  .topbar { padding: 0 14px; }
}
</style>
