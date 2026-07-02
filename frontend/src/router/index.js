import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../services/authStore.js'
import LoginPage                from '../views/LoginPage.vue'
import TableauDeBord            from '../views/TableauDeBord.vue'
import UtilisateursPage         from '../views/UtilisateursPage.vue'
import EntrepotsPage            from '../views/EntrepotsPage.vue'
import ZonesPage                from '../views/ZonesPage.vue'
import ProduitsPage             from '../views/ProduitsPage.vue'
import CategoriesPage           from '../views/CategoriesPage.vue'
import FournisseursPage         from '../views/FournisseursPage.vue'
import StocksPage               from '../views/StocksPage.vue'
import MouvementsStockPage      from '../views/MouvementsStockPage.vue'
import BonReceptionsPage        from '../views/BonReceptionsPage.vue'
import BonSortiesPage           from '../views/BonSortiesPage.vue'
import TransfertsPage           from '../views/TransfertsPage.vue'
// Modules 11–16
import InventairesPage           from '../views/InventairesPage.vue'
import AlertesPage               from '../views/AlertesPage.vue'
import ReportingPage             from '../views/ReportingPage.vue'
import TracabilitePage           from '../views/TracabilitePage.vue'
import CommandesFournisseursPage from '../views/CommandesFournisseursPage.vue'
// Module 17
import EmplacementsPage from '../views/EmplacementsPage.vue'
// Module 17
import EmplacementsPage          from '../views/EmplacementsPage.vue'
// Module 18
import QrCodePage                from '../views/QrCodePage.vue'
import ScanRedirectPage          from '../views/ScanRedirectPage.vue'

const routes = [
  // ── Publiques ────────────────────────────────────────────────────────────
  { path: '/login', component: LoginPage, meta: { public: true } },

  { path: '/', redirect: '/tableau-de-bord' },
  { path: '/tableau-de-bord',       component: TableauDeBord,              meta: { requiertAuth: true } },
  { path: '/entrepots',             component: EntrepotsPage,              meta: { requiertAuth: true } },
  { path: '/zones',                 component: ZonesPage,                  meta: { requiertAuth: true } },
  { path: '/produits',              component: ProduitsPage,               meta: { requiertAuth: true } },
  { path: '/categories',            component: CategoriesPage,             meta: { requiertAuth: true } },
  { path: '/fournisseurs',          component: FournisseursPage,           meta: { requiertAuth: true } },
  { path: '/stocks',                component: StocksPage,                 meta: { requiertAuth: true } },
  { path: '/mouvements-stock',      component: MouvementsStockPage,        meta: { requiertAuth: true } },
  { path: '/bon-receptions',        component: BonReceptionsPage,          meta: { requiertAuth: true } },
  { path: '/bon-sorties',           component: BonSortiesPage,             meta: { requiertAuth: true } },
  // Transferts : MAGASINIER inclus
  { path: '/transferts',            component: TransfertsPage,             meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE','MAGASINIER'] } },
  // Route spéciale : QR scanné sur téléphone → redirige selon connexion
  { path: '/scan',  component: ScanRedirectPage, meta: { public: true } },

  // ── Pages protégées ──────────────────────────────────────────────────────
  { path: '/',                  redirect: '/tableau-de-bord' },
  { path: '/tableau-de-bord',   component: TableauDeBord,   meta: { requiertAuth: true } },
  { path: '/entrepots',         component: EntrepotsPage,   meta: { requiertAuth: true } },
  { path: '/zones',             component: ZonesPage,       meta: { requiertAuth: true } },
  { path: '/produits',          component: ProduitsPage,    meta: { requiertAuth: true } },
  { path: '/categories',        component: CategoriesPage,  meta: { requiertAuth: true } },
  { path: '/fournisseurs',      component: FournisseursPage,meta: { requiertAuth: true } },
  { path: '/stocks',            component: StocksPage,      meta: { requiertAuth: true } },
  { path: '/mouvements-stock',  component: MouvementsStockPage, meta: { requiertAuth: true } },
  { path: '/bon-receptions',    component: BonReceptionsPage,   meta: { requiertAuth: true } },
  { path: '/bon-sorties',       component: BonSortiesPage,      meta: { requiertAuth: true } },

  // MAGASINIER inclus pour transferts
  { path: '/transferts', component: TransfertsPage,
    meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE','MAGASINIER'] } },

  // ── Modules 11–16 ─────────────────────────────────────────────────────────
  { path: '/inventaires',           component: InventairesPage,            meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE'] } },
  { path: '/alertes',               component: AlertesPage,                meta: { requiertAuth: true } },
  { path: '/reporting',             component: ReportingPage,              meta: { requiertAuth: true } },
  { path: '/tracabilite',           component: TracabilitePage,            meta: { requiertAuth: true, roles: ['ADMIN','AUDITEUR'] } },
  { path: '/commandes-fournisseurs',component: CommandesFournisseursPage,  meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE'] } },
  // Module 17 — emplacements : ADMIN + GESTIONNAIRE créent, MAGASINIER consulte
  { path: '/emplacements', component: EmplacementsPage, meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE','MAGASINIER'] } },
  // ── Modules 11–16 ────────────────────────────────────────────────────────
  { path: '/inventaires',
    component: InventairesPage,
    meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE'] } },
  { path: '/alertes',           component: AlertesPage,    meta: { requiertAuth: true } },
  { path: '/reporting',         component: ReportingPage,  meta: { requiertAuth: true } },
  { path: '/tracabilite',
    component: TracabilitePage,
    meta: { requiertAuth: true, roles: ['ADMIN','AUDITEUR'] } },
  { path: '/commandes-fournisseurs',
    component: CommandesFournisseursPage,
    meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE'] } },

  // ── Module 17 — Emplacements ─────────────────────────────────────────────
  // MAGASINIER peut consulter
  { path: '/emplacements',
    component: EmplacementsPage,
    meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE','MAGASINIER'] } },

  // ── Module 18 — QR Code ──────────────────────────────────────────────────
  { path: '/qrcode', component: QrCodePage, meta: { requiertAuth: true } },

  // ── Administration ADMIN ──────────────────────────────────────────────────
  { path: '/utilisateurs',
    component: UtilisateursPage,
    meta: { requiertAuth: true, roles: ['ADMIN'] } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.meta.public) return next()
  if (!authStore.estConnecte) {
    // Sauvegarder la destination pour redirection après connexion
    sessionStorage.setItem('sm_redirect_after_login', to.fullPath)
    return next('/login')
  }
  if (to.meta.roles) {
    const aAcces = to.meta.roles.some(r => authStore.aRole(r))
    if (!aAcces) return next('/tableau-de-bord')
  }
  next()
})

export default router
