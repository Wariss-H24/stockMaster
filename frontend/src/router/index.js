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
import InventairesPage          from '../views/InventairesPage.vue'
import AlertesPage              from '../views/AlertesPage.vue'
import ReportingPage            from '../views/ReportingPage.vue'
import TracabilitePage          from '../views/TracabilitePage.vue'
import CommandesFournisseursPage from '../views/CommandesFournisseursPage.vue'
import EmplacementsPage         from '../views/EmplacementsPage.vue'
import QrCodePage               from '../views/QrCodePage.vue'
import ScanRedirectPage         from '../views/ScanRedirectPage.vue'

const routes = [
  { path: '/login',                   component: LoginPage,                 meta: { public: true } },
  { path: '/scan',                    component: ScanRedirectPage,          meta: { public: true } },

  { path: '/',                        redirect: '/tableau-de-bord' },
  { path: '/tableau-de-bord',         component: TableauDeBord,             meta: { requiertAuth: true } },
  { path: '/entrepots',               component: EntrepotsPage,             meta: { requiertAuth: true } },
  { path: '/zones',                   component: ZonesPage,                 meta: { requiertAuth: true } },
  { path: '/produits',                component: ProduitsPage,              meta: { requiertAuth: true } },
  { path: '/categories',              component: CategoriesPage,            meta: { requiertAuth: true } },
  { path: '/fournisseurs',            component: FournisseursPage,          meta: { requiertAuth: true } },
  { path: '/stocks',                  component: StocksPage,                meta: { requiertAuth: true } },
  { path: '/mouvements-stock',        component: MouvementsStockPage,       meta: { requiertAuth: true } },
  { path: '/bon-receptions',          component: BonReceptionsPage,         meta: { requiertAuth: true } },
  { path: '/bon-sorties',             component: BonSortiesPage,            meta: { requiertAuth: true } },
  { path: '/alertes',                 component: AlertesPage,               meta: { requiertAuth: true } },
  { path: '/reporting',               component: ReportingPage,             meta: { requiertAuth: true } },
  { path: '/qrcode',                  component: QrCodePage,                meta: { requiertAuth: true } },

  { path: '/transferts',              component: TransfertsPage,            meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE','MAGASINIER'] } },
  { path: '/emplacements',            component: EmplacementsPage,          meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE','MAGASINIER'] } },
  { path: '/inventaires',             component: InventairesPage,           meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE'] } },
  { path: '/tracabilite',             component: TracabilitePage,           meta: { requiertAuth: true, roles: ['ADMIN','AUDITEUR'] } },
  { path: '/commandes-fournisseurs',  component: CommandesFournisseursPage, meta: { requiertAuth: true, roles: ['ADMIN','GESTIONNAIRE'] } },
  { path: '/utilisateurs',            component: UtilisateursPage,          meta: { requiertAuth: true, roles: ['ADMIN'] } },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  if (to.meta.public) return next()

  if (!authStore.estConnecte) {
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
