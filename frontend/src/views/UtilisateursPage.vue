<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Utilisateurs</h1>
      <p class="page-subtitle">Gestion des comptes et des rôles — Module 1</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Nom, username..." />
        </div>
        <!-- Page uniquement ADMIN → bouton toujours visible ici -->
        <button class="btn btn-primary" @click="ouvrirModal()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouvel utilisateur
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Utilisateur</th><th>Username</th><th>Rôles</th><th>Statut</th>
              <th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="5" style="text-align:center;padding:32px;color:var(--gray-400);">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="5" style="text-align:center;padding:32px;color:var(--gray-400);">Aucun utilisateur trouvé.</td>
            </tr>
            <tr v-for="u in listeFiltree" :key="u.id">
              <td>
                <div style="display:flex;align-items:center;gap:10px;">
                  <div style="width:36px;height:36px;border-radius:50%;background:var(--navy-xlight);color:var(--navy);display:flex;align-items:center;justify-content:center;font-weight:700;font-size:.85rem;flex-shrink:0;">
                    {{ initiales(u.nomComplet) }}
                  </div>
                  <span style="font-weight:600;color:var(--gray-900);">{{ u.nomComplet }}</span>
                </div>
              </td>
              <td><code style="color:var(--gray-500);font-size:.82rem;">{{ u.username }}</code></td>
              <td>
                <span v-for="r in u.roles" :key="r" class="badge badge-navy" style="margin-right:4px;">{{ r }}</span>
                <span v-if="!u.roles || u.roles.length === 0" style="color:var(--gray-400);">—</span>
              </td>
              <td>
                <span class="badge" :class="u.actif ? 'badge-success' : 'badge-danger'">
                  {{ u.actif ? 'Actif' : 'Inactif' }}
                </span>
              </td>
              <td style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <button class="btn btn-outline btn-sm" @click="ouvrirModal(u)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="1.8"/>
                    </svg>
                    Éditer
                  </button>
                  <!-- Ne pas pouvoir se désactiver soi-même -->
                  <button
                    v-if="u.actif && u.username !== moi"
                    class="btn btn-danger btn-sm"
                    @click="demanderDesactivation(u)"
                  >
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                    </svg>
                    Désactiver
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal formulaire -->
    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ form.id ? 'Modifier l\'utilisateur' : 'Nouvel utilisateur' }}</h3>
          <button class="modal-close" @click="modal = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">Nom complet *</label>
            <input class="form-input" v-model="form.nomComplet" placeholder="Jean Dupont" />
          </div>
          <div class="form-group">
            <label class="form-label">Username *</label>
            <input class="form-input" v-model="form.username" placeholder="jdupont" />
          </div>
          <div class="form-group">
            <label class="form-label">
              Mot de passe
              <span v-if="form.id" style="font-weight:400;color:var(--gray-400);"> (laisser vide pour ne pas changer)</span>
            </label>
            <input class="form-input" type="password" v-model="form.motDePasse" placeholder="••••••••" />
          </div>
          <div class="form-group">
            <label class="form-label">Rôle</label>
            <select class="form-select" v-model="roleSelectionne">
              <option value="">Aucun rôle</option>
              <option v-for="r in rolesDisponibles" :key="r" :value="r">{{ r }}</option>
            </select>
          </div>
          <div class="form-group" v-if="form.id">
            <label class="form-check">
              <input type="checkbox" v-model="form.actif" />
              <span>Compte actif</span>
            </label>
          </div>
          <div class="form-error" v-if="erreur">{{ erreur }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modal = false">Annuler</button>
          <button class="btn btn-primary" @click="sauvegarder">Sauvegarder</button>
        </div>
      </div>
    </div>

    <!-- Modale de confirmation désactivation -->
    <ConfirmModal
      v-if="confirm.visible"
      titre="Désactiver l'utilisateur"
      :message="`Voulez-vous désactiver le compte de « ${confirm.cible?.nomComplet} » (@${confirm.cible?.username}) ? Il ne pourra plus se connecter.`"
      type="warning"
      label-confirmer="Désactiver"
      @confirmer="confirmerDesactivation"
      @annuler="confirm.visible = false"
    />
  </div>
</template>

<script>
import { utilisateurApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'
import ConfirmModal from '../components/ConfirmModal.vue'

export default {
  name: 'UtilisateursPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], chargement: true, modal: false, erreur: '',
      recherche: '', roleSelectionne: '',
      confirm: { visible: false, cible: null },
      rolesDisponibles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'],
      form: { id: null, nomComplet: '', username: '', motDePasse: '', actif: true, roles: [] }
    }
  },
  computed: {
    // Username de l'utilisateur connecté (pour empêcher l'auto-désactivation)
    moi() { return authStore.user?.username },
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(u => u.nomComplet?.toLowerCase().includes(q) || u.username?.toLowerCase().includes(q))
    }
  },
  async mounted() { await this.charger() },
  methods: {
    initiales(nom) {
      if (!nom) return '?'
      return nom.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase()
    },
    async charger() {
      this.chargement = true
      try { const r = await utilisateurApi.findAll(); this.liste = r.data }
      finally { this.chargement = false }
    },
    ouvrirModal(u = null) {
      this.erreur = ''
      this.roleSelectionne = u?.roles?.[0] || ''
      this.form = u
        ? { id: u.id, nomComplet: u.nomComplet, username: u.username, motDePasse: '', actif: u.actif, roles: [...(u.roles || [])] }
        : { id: null, nomComplet: '', username: '', motDePasse: '', actif: true, roles: [] }
      this.modal = true
    },
    async sauvegarder() {
      this.erreur = ''

      // Validation : mot de passe obligatoire à la création
      if (!this.form.id && !this.form.motDePasse.trim()) {
        this.erreur = 'Le mot de passe est obligatoire pour un nouvel utilisateur'
        return
      }
      if (!this.form.username.trim()) {
        this.erreur = 'Le nom d\'utilisateur est obligatoire'
        return
      }
      if (!this.form.nomComplet.trim()) {
        this.erreur = 'Le nom complet est obligatoire'
        return
      }

      // Construction du payload — ne pas envoyer motDePasse vide en modification
      const payload = {
        nomComplet: this.form.nomComplet.trim(),
        username:   this.form.username.trim(),
        actif:      this.form.actif,
        roles:      this.roleSelectionne ? [this.roleSelectionne] : []
      }
      // Inclure le mot de passe seulement s'il est rempli
      if (this.form.motDePasse.trim()) {
        payload.motDePasse = this.form.motDePasse
      }

      console.log('[UTILISATEUR] Payload envoyé:', JSON.stringify(payload))

      try {
        if (this.form.id) await utilisateurApi.modifier(this.form.id, payload)
        else await utilisateurApi.creer(payload)
        this.modal = false
        this.charger()
      } catch (e) {
        console.error('[UTILISATEUR] Erreur:', JSON.stringify(e.response?.data))
        const data = e.response?.data
        if (data?.erreurs) {
          this.erreur = Object.values(data.erreurs).join(' — ')
        } else {
          this.erreur = data?.message || 'Erreur lors de la sauvegarde.'
        }
      }
    },
    demanderDesactivation(u) {
      this.confirm = { visible: true, cible: u }
    },
    async confirmerDesactivation() {
      await utilisateurApi.desactiver(this.confirm.cible.id)
      this.confirm = { visible: false, cible: null }
      this.charger()
    }
  }
}
</script>
