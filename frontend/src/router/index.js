import { createRouter, createWebHistory } from 'vue-router'
import TableauDeBord from '../views/TableauDeBord.vue'
import UtilisateursPage from '../views/UtilisateursPage.vue'
import EntrepotsPage from '../views/EntrepotsPage.vue'
import ZonesPage from '../views/ZonesPage.vue'
import ProduitsPage from '../views/ProduitsPage.vue'

const routes = [
  { path: '/', redirect: '/tableau-de-bord' },
  { path: '/tableau-de-bord', component: TableauDeBord },
  { path: '/utilisateurs', component: UtilisateursPage },
  { path: '/entrepots', component: EntrepotsPage },
  { path: '/zones', component: ZonesPage },
  { path: '/produits', component: ProduitsPage }
]

const router = createRouter({ history: createWebHistory(), routes })
export default router
