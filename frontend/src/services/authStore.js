import { reactive } from 'vue'

/**
 * Store d'authentification global (réactif, sans Pinia/Vuex).
 * Persiste le token et l'utilisateur dans localStorage.
 */
const storedUser = localStorage.getItem('sm_user')

export const authStore = reactive({
  token: localStorage.getItem('sm_token') || null,
  user: storedUser ? JSON.parse(storedUser) : null,

  // Connexion : sauvegarde token + user
  login(token, user) {
    this.token = token
    // S'assurer que roles est toujours un tableau (le backend renvoie un Set Java → peut être un objet)
    this.user = { ...user, roles: Array.isArray(user.roles) ? user.roles : Array.from(user.roles || []) }
    localStorage.setItem('sm_token', token)
    localStorage.setItem('sm_user', JSON.stringify(this.user))
  },

  // Déconnexion : nettoie tout
  logout() {
    this.token = null
    this.user = null
    localStorage.removeItem('sm_token')
    localStorage.removeItem('sm_user')
  },

  get estConnecte() {
    return !!this.token
  },

  // Vérifie si l'utilisateur a un rôle donné
  aRole(role) {
    return this.user?.roles?.includes(role) ?? false
  },

  // Vérifie si l'utilisateur a au moins un des rôles donnés
  aUnRole(...roles) {
    return roles.some(r => this.aRole(r))
  }
})
