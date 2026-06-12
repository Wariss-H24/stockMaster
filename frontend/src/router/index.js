import { createRouter, createWebHistory } from 'vue-router'
import UsersPage from '../views/UsersPage.vue'
import WarehousesPage from '../views/WarehousesPage.vue'
import ZonesPage from '../views/ZonesPage.vue'
import ProductsPage from '../views/ProductsPage.vue'

const routes = [
  { path: '/', redirect: '/products' },
  { path: '/users', component: UsersPage },
  { path: '/warehouses', component: WarehousesPage },
  { path: '/zones', component: ZonesPage },
  { path: '/products', component: ProductsPage }
]

const router = createRouter({ history: createWebHistory(), routes })
export default router
