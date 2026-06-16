import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../services/authStore.js'
import LoginPage from '../views/LoginPage.vue'
import TableauDeBord from '../views/TableauDeBord.vue'
import UtilisateursPage from '../views/UtilisateursPage.vue'
import EntrepotsPage from '../views/EntrepotsPage.vue'
import ZonesPage from '../views/ZonesPage.vue'
import ProduitsPage from '../views/ProduitsPage.vue'
import CategoriesPage from '../views/CategoriesPage.vue'
import FournisseursPage from '../views/FournisseursPage.vue'
import StocksPage from '../views/StocksPage.vue'
import MouvementsStockPage from '../views/MouvementsStockPage.vue'
import BonReceptionsPage from '../views/BonReceptionsPage.vue'
import BonSortiesPage from '../views/BonSortiesPage.vue'

const routes = [
  // Page publique
  { path: '/login', component: LoginPage, meta: { public: true } },

  // Pages protégées
  { path: '/', redirect: '/tableau-de-bord' },
  { path: '/tableau-de-bord', component: TableauDeBord, meta: { requiertAuth: true } },
  { path: '/entrepots',   component: EntrepotsPage,   meta: { requiertAuth: true } },
  { path: '/zones',       component: ZonesPage,       meta: { requiertAuth: true } },
  { path: '/produits',    component: ProduitsPage,     meta: { requiertAuth: true } },
  { path: '/categories',  component: CategoriesPage,  meta: { requiertAuth: true } },
  { path: '/fournisseurs',component: FournisseursPage,meta: { requiertAuth: true } },
  { path: '/stocks',      component: StocksPage,      meta: { requiertAuth: true } },
  { path: '/mouvements-stock', component: MouvementsStockPage, meta: { requiertAuth: true } },
  { path: '/bon-receptions', component: BonReceptionsPage, meta: { requiertAuth: true } },
  { path: '/bon-sorties', component: BonSortiesPage, meta: { requiertAuth: true } },

  // Page réservée à l'ADMIN
  {
    path: '/utilisateurs',
    component: UtilisateursPage,
    meta: { requiertAuth: true, roles: ['ADMIN'] }
  }
]

const router = createRouter({ history: createWebHistory(), routes })

// Guard global : vérifie la connexion et les rôles avant chaque navigation
router.beforeEach((to, from, next) => {
  // Route publique → OK
  if (to.meta.public) return next()

  // Pas connecté → rediriger vers /login
  if (!authStore.estConnecte) return next('/login')

  // Rôle requis → vérifier
  if (to.meta.roles) {
    const aAcces = to.meta.roles.some(r => authStore.aRole(r))
    if (!aAcces) return next('/tableau-de-bord') // Accès refusé → dashboard
  }

  next()
})

export default router
