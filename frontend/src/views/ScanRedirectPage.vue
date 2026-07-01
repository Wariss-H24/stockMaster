<template>
  <div class="scan-redirect">
    <!-- Non connecté -->
    <div v-if="!estConnecte" class="auth-required">
      <div class="auth-icon">
        <svg width="52" height="52" viewBox="0 0 24 24" fill="none">
          <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" stroke-width="1.8"/>
          <path d="M7 11V7a5 5 0 0110 0v4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
      </div>
      <h1>Connexion requise</h1>
      <p>Vous devez être connecté pour accéder aux informations du QR Code.</p>
      <button class="btn btn-primary" @click="allerConnexion">Se connecter</button>
    </div>

    <!-- Chargement -->
    <div v-else-if="chargement" class="loading-state">
      <div class="spinner"></div>
      <p>Récupération des données...</p>
    </div>

    <!-- Erreur -->
    <div v-else-if="erreur" class="error-state">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" style="color:var(--danger);">
        <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
        <path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <p>{{ erreur }}</p>
      <router-link to="/" class="btn btn-outline">Retour à l'accueil</router-link>
    </div>

    <!-- Résultat : on redirige vers QrCodePage avec les données -->
    <div v-else>
      <p>Redirection...</p>
    </div>
  </div>
</template>

<script>
import { authStore } from '../services/authStore.js'
import { qrApi }     from '../services/api.js'

export default {
  name: 'ScanRedirectPage',
  data() {
    return { chargement: false, erreur: '' }
  },
  computed: {
    estConnecte() { return authStore.estConnecte }
  },
  async mounted() {
    if (!this.estConnecte) return
    await this.resoudre()
  },
  methods: {
    allerConnexion() {
      // Sauvegarder l'URL pour revenir après connexion
      sessionStorage.setItem('sm_redirect_after_login', window.location.href)
      this.$router.push('/login')
    },
    async resoudre() {
      const params = new URLSearchParams(window.location.search)
      const type   = params.get('type')
      const id     = params.get('id')
      if (!type || !id) {
        this.erreur = 'QR Code invalide ou incomplet.'
        return
      }
      this.chargement = true
      try {
        // On passe les params à QrCodePage via router
        this.$router.replace({ path: '/qrcode', query: { type, id } })
      } catch (e) {
        this.erreur = 'Données introuvables pour ce QR Code.'
      } finally {
        this.chargement = false
      }
    }
  }
}
</script>

<style scoped>
.scan-redirect {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: var(--gray-50); padding: 24px;
}
.auth-required, .loading-state, .error-state {
  text-align: center; max-width: 380px;
  background: var(--white); border-radius: var(--radius-lg);
  padding: 40px 32px; box-shadow: var(--shadow-lg);
}
.auth-icon { color: var(--navy); margin-bottom: 20px; }
h1 { font-size: 1.3rem; font-weight: 800; color: var(--gray-900); margin-bottom: 10px; }
p  { font-size: .9rem; color: var(--gray-600); line-height: 1.6; margin-bottom: 20px; }
.spinner {
  width: 44px; height: 44px; border: 4px solid var(--gray-200);
  border-top-color: var(--navy); border-radius: 50%;
  animation: spin .8s linear infinite; margin: 0 auto 16px;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
