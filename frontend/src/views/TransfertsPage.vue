<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Transferts inter-entrepôts</h1>
      <p class="page-subtitle">Déplacer des produits entre entrepôts — Module 10</p>
    </div>

    <!-- KPIs -->
    <div class="stats-grid" style="grid-template-columns: repeat(4,1fr); margin-bottom:24px;">
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--navy-xlight);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M4 12h7M13 6l5 6-5 6M21 7v10" stroke="var(--navy)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Total</p><p class="stat-value">{{ liste.length }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--warning-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="var(--warning)" stroke-width="1.8"/><path d="M12 7v5l3 3" stroke="var(--warning)" stroke-width="1.8" stroke-linecap="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Brouillons</p><p class="stat-value">{{ compteur('BROUILLON') }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--info-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M5 12h14M12 5l7 7-7 7" stroke="var(--info)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Expédiés</p><p class="stat-value">{{ compteur('EXPEDIE') }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--success-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M20 6L9 17l-5-5" stroke="var(--success)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Reçus</p><p class="stat-value">{{ compteur('RECU') }}</p></div>
      </div>
    </div>

    <!-- Tableau des transferts -->
    <div class="card" style="margin-bottom:24px;">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Référence, produit, entrepôt, statut..." />
        </div>
        <button class="btn btn-primary" @click="ouvrirModalCreation">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouveau transfert
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Référence</th>
              <th>Produit</th>
              <th>Source</th>
              <th>Destination</th>
              <th>Qté</th>
              <th>Statut</th>
              <th>Date création</th>
              <th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="8" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="8" class="empty-state">Aucun transfert trouvé.</td>
            </tr>
            <tr v-for="t in listeFiltree" :key="t.id">
              <td><code style="font-size:.82rem;background:var(--gray-100);padding:2px 6px;border-radius:4px;">{{ t.reference }}</code></td>
              <td>{{ t.produitNom }}</td>
              <td>
                <span>{{ t.entrepotSourceNom }}</span>
                <span v-if="t.zoneSourceNom" class="text-muted"> / {{ t.zoneSourceNom }}</span>
              </td>
              <td>
                <span>{{ t.entrepotDestinationNom }}</span>
                <span v-if="t.zoneDestinationNom" class="text-muted"> / {{ t.zoneDestinationNom }}</span>
              </td>
              <td><strong>{{ t.quantite }}</strong></td>
              <td><span class="badge" :class="badgeStatut(t.statut)">{{ labelStatut(t.statut) }}</span></td>
              <td>{{ formaterDate(t.dateCrea) }}</td>
              <td style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap;">
                  <button v-if="t.statut === 'BROUILLON'" class="btn btn-outline btn-sm" @click="demanderAction(t, 'expedier')">Expédier</button>
                  <button v-if="t.statut === 'EXPEDIE'"   class="btn btn-primary btn-sm" @click="demanderAction(t, 'recevoir')">Réceptionner</button>
                  <button v-if="t.statut === 'BROUILLON'" class="btn btn-danger btn-sm"  @click="demanderAction(t, 'annuler')">Annuler</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal : Nouveau transfert -->
    <div class="modal-overlay" v-if="modalCreation" @click.self="modalCreation = false">
      <div class="modal" style="max-width:600px;">
        <div class="modal-header">
          <h3 class="modal-title">Nouveau transfert</h3>
          <button class="modal-close" @click="modalCreation = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Produit *</label>
              <select class="form-select" v-model.number="form.produitId">
                <option value="">Sélectionner un produit</option>
                <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Quantité *</label>
              <input class="form-input" type="number" min="1" v-model.number="form.quantite" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Entrepôt source *</label>
              <select class="form-select" v-model.number="form.entrepotSourceId" @change="form.zoneSourceId = null">
                <option value="">Sélectionner</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Zone source <span class="text-muted">(optionnel)</span></label>
              <select class="form-select" v-model.number="form.zoneSourceId" :disabled="!form.entrepotSourceId">
                <option :value="null">Aucune zone</option>
                <option v-for="z in zonesSource" :key="z.id" :value="z.id">{{ z.nom }}</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Entrepôt destination *</label>
              <select class="form-select" v-model.number="form.entrepotDestinationId" @change="form.zoneDestinationId = null">
                <option value="">Sélectionner</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Zone destination <span class="text-muted">(optionnel)</span></label>
              <select class="form-select" v-model.number="form.zoneDestinationId" :disabled="!form.entrepotDestinationId">
                <option :value="null">Aucune zone</option>
                <option v-for="z in zonesDestination" :key="z.id" :value="z.id">{{ z.nom }}</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Commentaire</label>
            <textarea class="form-input" v-model="form.commentaire" rows="2" placeholder="Notes de transfert..."></textarea>
          </div>

          <div class="form-error" v-if="erreurModal">{{ erreurModal }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modalCreation = false">Annuler</button>
          <button class="btn btn-primary" @click="creerTransfert" :disabled="chargementAction">
            {{ chargementAction ? 'Création...' : 'Créer en brouillon' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal de confirmation d'action (expédier / réceptionner / annuler) -->
    <div class="modal-overlay" v-if="confirm.visible" @click.self="confirm.visible = false">
      <div class="modal" style="max-width:420px;">
        <div class="modal-header">
          <h3 class="modal-title">{{ confirm.titre }}</h3>
          <button class="modal-close" @click="confirm.visible = false">✕</button>
        </div>
        <div class="modal-body">
          <p style="color:var(--gray-700);font-size:.9rem;line-height:1.6;">{{ confirm.message }}</p>
          <div v-if="confirm.cible" style="margin-top:14px;background:var(--gray-50);border:1px solid var(--gray-200);border-radius:var(--radius-sm);padding:12px 14px;">
            <p style="font-size:.82rem;color:var(--gray-500);">Référence : <strong>{{ confirm.cible.reference }}</strong></p>
            <p style="font-size:.82rem;color:var(--gray-500);margin-top:4px;">Produit : <strong>{{ confirm.cible.produitNom }}</strong> — {{ confirm.cible.quantite }} unité(s)</p>
            <p style="font-size:.82rem;color:var(--gray-500);margin-top:4px;">{{ confirm.cible.entrepotSourceNom }} → {{ confirm.cible.entrepotDestinationNom }}</p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="confirm.visible = false">Annuler</button>
          <button :class="['btn', confirm.action === 'annuler' ? 'btn-danger' : 'btn-primary']"
                  @click="executerAction" :disabled="chargementAction">
            {{ chargementAction ? 'En cours...' : confirm.labelConfirmer }}
          </button>
        </div>
      </div>
    </div>

    <!-- Toast notification -->
    <transition name="toast-slide">
      <div v-if="toast.visible" :class="['toast-notif', toast.type === 'succes' ? 'toast-succes' : 'toast-erreur']">
        <div class="toast-icon">
          <svg v-if="toast.type === 'succes'" width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
            <path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="toast-body">
          <p class="toast-title">{{ toast.type === 'succes' ? 'Succès' : 'Erreur' }}</p>
          <p class="toast-message">{{ toast.message }}</p>
          <div class="toast-progress">
            <div class="toast-progress-bar" :style="{ animationDuration: toast.duree + 'ms' }"></div>
          </div>
        </div>
        <button class="toast-close" @click="fermerToast">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </transition>
  </div>
</template>

<script>
import { transfertApi, entrepotApi, zoneApi, produitApi } from '../services/api.js'

export default {
  name: 'TransfertsPage',
  data() {
    return {
      liste: [],
      entrepots: [],
      zones: [],
      produits: [],
      recherche: '',
      chargement: true,
      chargementAction: false,
      erreurModal: '',
      modalCreation: false,
      form: {
        produitId: null,
        entrepotSourceId: null,
        zoneSourceId: null,
        entrepotDestinationId: null,
        zoneDestinationId: null,
        quantite: 1,
        commentaire: ''
      },
      confirm: {
        visible: false,
        action: '',
        titre: '',
        message: '',
        labelConfirmer: '',
        cible: null
      },
      toast: { visible: false, type: 'succes', message: '', duree: 5000, _timer: null }
    }
  },
  computed: {
    listeFiltree() {
      const q = this.recherche.toLowerCase().trim()
      if (!q) return this.liste
      return this.liste.filter(t =>
        t.reference?.toLowerCase().includes(q) ||
        t.produitNom?.toLowerCase().includes(q) ||
        t.entrepotSourceNom?.toLowerCase().includes(q) ||
        t.entrepotDestinationNom?.toLowerCase().includes(q) ||
        t.statut?.toLowerCase().includes(q)
      )
    },
    zonesSource() {
      return this.zones.filter(z => z.entrepotId === this.form.entrepotSourceId)
    },
    zonesDestination() {
      return this.zones.filter(z => z.entrepotId === this.form.entrepotDestinationId)
    }
  },
  async mounted() {
    await Promise.all([this.charger(), this.chargerDonnees()])
  },
  beforeUnmount() {
    clearTimeout(this.toast._timer)
  },
  methods: {
    // --- Chargement ---
    async charger() {
      this.chargement = true
      try {
        const res = await transfertApi.findAll()
        this.liste = res.data
      } catch {
        this.afficherToast('Impossible de charger les transferts.', 'erreur')
      } finally {
        this.chargement = false
      }
    },
    async chargerDonnees() {
      const [e, z, p] = await Promise.all([
        entrepotApi.findAll(),
        zoneApi.findAll(),
        produitApi.findAll()
      ])
      this.entrepots = e.data
      this.zones     = z.data
      this.produits  = p.data
    },

    // --- Création ---
    ouvrirModalCreation() {
      this.erreurModal = ''
      this.form = {
        produitId: null,
        entrepotSourceId: null,
        zoneSourceId: null,
        entrepotDestinationId: null,
        zoneDestinationId: null,
        quantite: 1,
        commentaire: ''
      }
      this.modalCreation = true
    },
    async creerTransfert() {
      this.erreurModal = ''
      if (!this.form.produitId)             { this.erreurModal = 'Le produit est requis.' ; return }
      if (!this.form.entrepotSourceId)      { this.erreurModal = 'L\'entrepôt source est requis.' ; return }
      if (!this.form.entrepotDestinationId) { this.erreurModal = 'L\'entrepôt de destination est requis.' ; return }
      if (this.form.entrepotSourceId === this.form.entrepotDestinationId &&
          this.form.zoneSourceId === this.form.zoneDestinationId) {
        this.erreurModal = 'La source et la destination ne peuvent pas être identiques.'
        return
      }
      if (!this.form.quantite || this.form.quantite < 1) {
        this.erreurModal = 'La quantité doit être au moins 1.'
        return
      }
      this.chargementAction = true
      try {
        await transfertApi.creer(this.form)
        this.modalCreation = false
        await this.charger()
        this.afficherToast('Transfert créé en brouillon.', 'succes')
      } catch (e) {
        this.erreurModal = e.response?.data?.message || 'Impossible de créer le transfert.'
      } finally {
        this.chargementAction = false
      }
    },

    // --- Actions sur cycle de vie ---
    demanderAction(transfert, action) {
      const configs = {
        expedier: {
          titre: 'Expédier le transfert',
          message: 'Confirmer l\'expédition ? Le stock source sera débité et la marchandise passera en transit à destination.',
          labelConfirmer: 'Expédier'
        },
        recevoir: {
          titre: 'Réceptionner le transfert',
          message: 'Confirmer la réception ? La marchandise sortira du transit et sera créditée dans le stock de destination.',
          labelConfirmer: 'Réceptionner'
        },
        annuler: {
          titre: 'Annuler le transfert',
          message: 'Voulez-vous annuler ce transfert ? Cette action est irréversible.',
          labelConfirmer: 'Annuler le transfert'
        }
      }
      const cfg = configs[action]
      this.confirm = { visible: true, action, cible: transfert, ...cfg }
    },
    async executerAction() {
      const { action, cible } = this.confirm
      this.chargementAction = true
      try {
        if (action === 'expedier') await transfertApi.expedier(cible.id)
        else if (action === 'recevoir') await transfertApi.recevoir(cible.id)
        else if (action === 'annuler') await transfertApi.annuler(cible.id)
        this.confirm.visible = false
        await this.charger()
        const messages = {
          expedier: 'Transfert expédié — stock source débité.',
          recevoir: 'Transfert réceptionné — stock destination crédité.',
          annuler:  'Transfert annulé.'
        }
        this.afficherToast(messages[action], 'succes')
      } catch (e) {
        this.confirm.visible = false
        this.afficherToast(e.response?.data?.message || 'L\'opération a échoué.', 'erreur')
      } finally {
        this.chargementAction = false
      }
    },

    // --- Helpers affichage ---
    compteur(statut) {
      return this.liste.filter(t => t.statut === statut).length
    },
    badgeStatut(statut) {
      const map = {
        BROUILLON: 'badge-warning',
        EXPEDIE:   'badge-info',
        RECU:      'badge-success',
        ANNULE:    'badge-danger'
      }
      return map[statut] || 'badge-navy'
    },
    labelStatut(statut) {
      const map = {
        BROUILLON: 'Brouillon',
        EXPEDIE:   'Expédié',
        RECU:      'Reçu',
        ANNULE:    'Annulé'
      }
      return map[statut] || statut
    },
    formaterDate(date) {
      if (!date) return '—'
      const d = new Date(date)
      return isNaN(d) ? '—' : d.toLocaleString('fr-FR', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
      })
    },

    // --- Toast ---
    afficherToast(message, type = 'succes', duree = 5000) {
      clearTimeout(this.toast._timer)
      this.toast = { visible: true, type, message, duree, _timer: null }
      this.toast._timer = setTimeout(() => { this.toast.visible = false }, duree)
    },
    fermerToast() {
      clearTimeout(this.toast._timer)
      this.toast.visible = false
    }
  }
}
</script>

<style scoped>
.text-muted { color: var(--gray-400); font-size: .82rem; }
.empty-state { text-align: center; padding: 40px; color: var(--gray-400); }

/* Toast */
.toast-notif {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  padding: 16px 14px 12px 16px;
  max-width: 420px;
  min-width: 300px;
}
.toast-succes {
  background: #f0fdf4;
  border: 1px solid #86efac;
  border-left: 4px solid var(--success);
}
.toast-succes .toast-icon  { color: var(--success); }
.toast-succes .toast-title { color: var(--success); }
.toast-succes .toast-progress-bar { background: var(--success); }
.toast-succes .toast-progress { background: rgba(22,163,74,.12); }

.toast-erreur {
  background: #fff5f5;
  border: 1px solid #fca5a5;
  border-left: 4px solid var(--danger);
}
.toast-erreur .toast-icon  { color: var(--danger); }
.toast-erreur .toast-title { color: var(--danger); }
.toast-erreur .toast-progress-bar { background: var(--danger); }
.toast-erreur .toast-progress { background: rgba(220,38,38,.12); }

.toast-icon  { flex-shrink: 0; margin-top: 1px; }
.toast-body  { flex: 1; min-width: 0; }
.toast-title { font-weight: 700; font-size: .88rem; margin-bottom: 3px; }
.toast-message { font-size: .83rem; color: var(--gray-700); line-height: 1.4; }
.toast-progress { margin-top: 8px; height: 3px; border-radius: 999px; overflow: hidden; }
.toast-progress-bar {
  width: 100%;
  height: 100%;
  border-radius: 999px;
  animation: toast-drain linear forwards;
}
.toast-close {
  background: none; border: none; cursor: pointer;
  color: var(--gray-400); padding: 2px; flex-shrink: 0;
  border-radius: 4px; display: flex; align-items: center;
}
.toast-close:hover { color: var(--gray-700); background: var(--gray-100); }
@keyframes toast-drain { from { width: 100%; } to { width: 0%; } }

.toast-slide-enter-active { transition: all .25s ease; }
.toast-slide-leave-active { transition: all .2s ease; }
.toast-slide-enter-from   { opacity: 0; transform: translateX(40px); }
.toast-slide-leave-to     { opacity: 0; transform: translateX(40px); }
</style>
