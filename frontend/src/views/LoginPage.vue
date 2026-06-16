<template>
  <div class="auth-layout">
    <div class="auth-card">

      <!-- Logo -->
      <div class="auth-brand">
        <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
          <rect width="36" height="36" rx="8" fill="#1e3a5f"/>
          <path d="M8 13h20M8 18h13M8 23h16" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/>
        </svg>
        <span>StockMaster</span>
      </div>

      <!-- Titre -->
      <h1 class="auth-title">{{ mode === 'login' ? 'Connexion' : 'Créer un compte' }}</h1>
      <p class="auth-subtitle">
        {{ mode === 'login' ? 'Connectez-vous à votre espace de gestion' : 'Remplissez le formulaire pour créer un compte' }}
      </p>

      <!-- Formulaire -->
      <form @submit.prevent="soumettre">

        <div class="form-group" v-if="mode === 'register'">
          <label class="form-label">Nom complet</label>
          <input class="form-input" v-model="form.nomComplet" placeholder="Jean Dupont" autocomplete="name" />
        </div>

        <div class="form-group">
          <label class="form-label">Nom d'utilisateur</label>
          <input class="form-input" v-model="form.username" placeholder="admin" autocomplete="username" />
        </div>

        <div class="form-group">
          <label class="form-label">Mot de passe</label>
          <div class="input-with-icon">
            <input class="form-input" :type="voirMdp ? 'text' : 'password'" v-model="form.motDePasse" placeholder="••••••••" autocomplete="current-password" />
            <button type="button" class="eye-btn" @click="voirMdp = !voirMdp">
              <!-- Oeil ouvert -->
              <svg v-if="!voirMdp" width="17" height="17" viewBox="0 0 24 24" fill="none">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="1.8"/>
                <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.8"/>
              </svg>
              <!-- Oeil barré -->
              <svg v-else width="17" height="17" viewBox="0 0 24 24" fill="none">
                <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                <path d="M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-error" v-if="erreur">{{ erreur }}</div>

        <button type="submit" class="btn btn-primary btn-full" :disabled="chargement">
          <span v-if="chargement">Chargement...</span>
          <span v-else>{{ mode === 'login' ? 'Se connecter' : 'Créer le compte' }}</span>
        </button>
      </form>

      <!-- Switcher login/register -->
      <p class="auth-switch">
        <span v-if="mode === 'login'">
          Pas encore de compte ?
          <a href="#" @click.prevent="basculer">S'inscrire</a>
        </span>
        <span v-else>
          Déjà un compte ?
          <a href="#" @click.prevent="basculer">Se connecter</a>
        </span>
      </p>

      <!-- Comptes de démo -->
      <div class="demo-box" v-if="mode === 'login'">
        <p class="demo-title">Comptes de démonstration</p>
        <div class="demo-grid">
          <button v-for="u in demoCreds" :key="u.username" class="demo-btn" @click="remplir(u)">
            <span class="demo-role">{{ u.role }}</span>
            <span class="demo-user">{{ u.username }} / {{ u.mdp }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { authApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'

export default {
  name: 'LoginPage',
  data() {
    return {
      mode: 'login',
      voirMdp: false,
      chargement: false,
      erreur: '',
      form: { username: '', motDePasse: '', nomComplet: '' },
      demoCreds: [
        { role: 'ADMIN',        username: 'admin',        mdp: 'admin123'  },
        { role: 'GESTIONNAIRE', username: 'gestionnaire', mdp: 'gest123'   },
        { role: 'MAGASINIER',   username: 'magasinier',   mdp: 'mag123'    },
        { role: 'AUDITEUR',     username: 'auditeur',     mdp: 'audit123'  },
      ]
    }
  },
  methods: {
    basculer() {
      this.mode = this.mode === 'login' ? 'register' : 'login'
      this.erreur = ''
      this.form = { username: '', motDePasse: '', nomComplet: '' }
    },
    remplir(u) {
      this.form.username = u.username
      this.form.motDePasse = u.mdp
    },
    async soumettre() {
      // Validation minimale côté client avant d'envoyer
      if (!this.form.username.trim()) { this.erreur = 'Le nom d\'utilisateur est obligatoire'; return }
      if (!this.form.motDePasse.trim()) { this.erreur = 'Le mot de passe est obligatoire'; return }
      if (this.mode === 'register' && !this.form.nomComplet.trim()) { this.erreur = 'Le nom complet est obligatoire'; return }

      this.erreur = ''
      this.chargement = true

      const payload = this.mode === 'login'
        ? { username: this.form.username.trim(), motDePasse: this.form.motDePasse }
        : { username: this.form.username.trim(), motDePasse: this.form.motDePasse, nomComplet: this.form.nomComplet.trim() }

      try {
        const res = await authApi[this.mode === 'login' ? 'login' : 'register'](payload)
        authStore.login(res.data.token, {
          username: res.data.username,
          nomComplet: res.data.nomComplet,
          roles: Array.isArray(res.data.roles) ? res.data.roles : Array.from(res.data.roles || [])
        })
        this.$router.push('/tableau-de-bord')
      } catch (e) {
        const data = e.response?.data
        if (data?.erreurs) {
          this.erreur = Object.values(data.erreurs).join(' — ')
        } else {
          this.erreur = data?.message || 'Identifiants incorrects'
        }
      } finally {
        this.chargement = false
      }
    }
  }
}
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  background: var(--gray-100);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.auth-card {
  background: var(--white);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 40px 36px;
  width: 100%;
  max-width: 420px;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--navy);
}

.auth-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--gray-900);
  margin-bottom: 6px;
}
.auth-subtitle {
  font-size: .85rem;
  color: var(--gray-500);
  margin-bottom: 24px;
}

.input-with-icon { position: relative; }
.input-with-icon .form-input { padding-right: 40px; }
.eye-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--gray-400);
  display: flex;
  align-items: center;
  padding: 2px;
}
.eye-btn:hover { color: var(--gray-700); }

.btn-full { width: 100%; justify-content: center; padding: 10px; font-size: .9rem; margin-top: 6px; }
.btn:disabled { opacity: .6; cursor: not-allowed; }

.auth-switch {
  text-align: center;
  margin-top: 20px;
  font-size: .85rem;
  color: var(--gray-500);
}
.auth-switch a { color: var(--navy); font-weight: 600; text-decoration: none; }
.auth-switch a:hover { text-decoration: underline; }

/* Comptes de démo */
.demo-box {
  margin-top: 24px;
  border-top: 1px solid var(--gray-200);
  padding-top: 18px;
}
.demo-title {
  font-size: .72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .06em;
  color: var(--gray-400);
  margin-bottom: 10px;
}
.demo-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.demo-btn {
  background: var(--gray-50);
  border: 1px solid var(--gray-200);
  border-radius: var(--radius-sm);
  padding: 7px 10px;
  cursor: pointer;
  text-align: left;
  transition: background .15s;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.demo-btn:hover { background: var(--navy-xlight); border-color: var(--navy); }
.demo-role { font-size: .68rem; font-weight: 700; color: var(--navy); text-transform: uppercase; }
.demo-user { font-size: .72rem; color: var(--gray-500); }
</style>
