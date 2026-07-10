<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Bons de réception</h1>
      <p class="page-subtitle">Traçabilité des arrivées de marchandises — Module 8</p>
    </div>

    <!-- Toast erreur auto-fermant -->
    <transition name="toast-slide">
      <div v-if="toast.visible" class="toast-error">
        <div class="toast-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/>
            <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="toast-body">
          <p class="toast-title">Erreur</p>
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

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Rechercher par fournisseur, entrepôt, statut..." />
        </div>
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirModal()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouveau bon
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Fournisseur</th><th>Entrepôt</th><th>Emplacement</th><th>Statut</th><th>Date</th><th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="6" class="empty-state">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="6" class="empty-state">Aucun bon de réception trouvé.</td>
            </tr>
            <tr v-for="b in listeFiltree" :key="b.id">
              <td>{{ b.fournisseurNom || b.fournisseurId }}</td>
              <td>{{ b.entrepotNom || b.entrepotId }}</td>
              <td>
                <span v-if="b.emplacementCodeComplet" class="ref-code">{{ b.emplacementCodeComplet }}</span>
                <span v-else-if="b.zoneNom" style="color:var(--gray-500);">{{ b.zoneNom }}</span>
                <span v-else style="color:var(--gray-300);">—</span>
              </td>
              <td>
                <span class="badge" :class="b.statut === 'VALIDE' ? 'badge-success' : 'badge-warning'">{{ b.statut }}</span>
              </td>
              <td>{{ formaterDate(b.date) }}</td>
              <td style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <button class="btn btn-outline btn-sm" @click="ouvrirModal(b)">Voir</button>
                  <button v-if="peutEcrire && b.statut === 'BROUILLON'" class="btn btn-primary btn-sm" @click="valider(b)">Valider</button>
                  <button v-if="peutSupprimer && b.statut === 'BROUILLON'" class="btn btn-danger btn-sm" @click="demanderSuppression(b)">Supprimer</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal formulaire -->
    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal large-modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ form.id ? 'Modifier le bon de réception' : 'Nouveau bon de réception' }}</h3>
          <button class="modal-close" @click="modal = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Fournisseur *</label>
              <select class="form-select" v-model.number="form.fournisseurId">
                <option value="">Sélectionner</option>
                <option v-for="f in fournisseurs" :key="f.id" :value="f.id">{{ f.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Entrepôt *</label>
              <select class="form-select" v-model.number="form.entrepotId">
                <option value="">Sélectionner</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Zone</label>
              <select class="form-select" v-model.number="form.zoneId" @change="form.rayonId = null; form.etagereId = null; form.emplacementId = null">
                <option :value="null">— Aucune zone —</option>
                <option v-for="z in zonesEntrepot" :key="z.id" :value="z.id">{{ z.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Rayon <span class="form-hint">(optionnel)</span></label>
              <select class="form-select" v-model.number="form.rayonId" :disabled="!form.zoneId"
                @change="form.etagereId = null; form.emplacementId = null">
                <option :value="null">— Aucun rayon —</option>
                <option v-for="r in rayonsZone" :key="r.id" :value="r.id">{{ r.code }} — {{ r.nom }}</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Étagère <span class="form-hint">(optionnel)</span></label>
              <select class="form-select" v-model.number="form.etagereId" :disabled="!form.rayonId"
                @change="form.emplacementId = null">
                <option :value="null">— Aucune étagère —</option>
                <option v-for="e in etagereRayon" :key="e.id" :value="e.id">{{ e.code }} — {{ e.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Emplacement <span class="form-hint">(optionnel)</span></label>
              <select class="form-select" v-model.number="form.emplacementId" :disabled="!form.etagereId">
                <option :value="null">— Aucun emplacement —</option>
                <option v-for="emp in emplacementsEtagere" :key="emp.id" :value="emp.id">
                  {{ emp.code }} — {{ emp.nom }}
                  <template v-if="emp.capaciteMax">
                    ({{ emp.capaciteMax - emp.capaciteUtilisee }} places dispo)
                  </template>
                </option>
              </select>
            </div>
          </div>
          <!-- Affichage du chemin complet sélectionné -->
          <div v-if="form.emplacementId" class="emplacement-recap">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="1.8"/></svg>
            <span>Emplacement : <strong>{{ emplacementSelectionne?.codeComplet || cheminEmplacement }}</strong></span>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Contrôle qualité</label>
              <label class="form-check">
                <input type="checkbox" v-model="form.controleQualiteOk" />
                <span>Contrôle validé</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Commentaire</label>
            <textarea class="form-input" v-model="form.commentaire" rows="3" placeholder="Notes d'arrivée"></textarea>
          </div>
          <div class="line-table">
            <div class="line-header">Lignes de réception</div>
            <div v-if="form.lignes.length === 0" class="empty-state">Ajoutez au moins une ligne de produit.</div>
            <div v-for="(ligne, index) in form.lignes" :key="index" class="line-row">
              <div class="form-row align-end">
                <div class="form-group">
                  <label class="form-label">Produit *</label>
                  <select class="form-select" v-model.number="ligne.produitId">
                    <option value="">Sélectionner</option>
                    <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.nom }}</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Quantité *</label>
                  <input class="form-input" type="number" min="1" v-model.number="ligne.quantite" />
                </div>
                <div class="form-group">
                  <label class="form-label">Prix achat</label>
                  <input class="form-input" type="number" min="0" step="0.01" v-model.number="ligne.prixAchat" />
                </div>
                <button class="btn btn-danger btn-sm" style="height:36px;align-self:flex-end;" @click.prevent="supprimerLigne(index)">Supprimer</button>
              </div>
            </div>
            <button class="btn btn-outline" @click.prevent="ajouterLigne">Ajouter une ligne</button>
          </div>
          <div class="form-error" v-if="erreur">{{ erreur }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modal = false">Annuler</button>
          <button class="btn btn-primary" @click="sauvegarder">Sauvegarder</button>
        </div>
      </div>
    </div>

    <ConfirmModal
      v-if="confirm.visible"
      titre="Supprimer le bon de réception"
      :message="`Voulez-vous supprimer le bon #${confirm.cible?.id} (${confirm.cible?.fournisseurNom}) ? Cette action est irréversible.`"
      type="danger"
      label-confirmer="Supprimer"
      @confirmer="confirmerSuppression"
      @annuler="confirm.visible = false"
    />
  </div>
</template>

<script>
import { receptionApi, fournisseurApi, entrepotApi, zoneApi, produitApi, rayonApi, etagereApi, emplacementApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'
import ConfirmModal from '../components/ConfirmModal.vue'

export default {
  name: 'BonReceptionsPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], fournisseurs: [], entrepots: [], zones: [], produits: [],
      rayons: [], etageres: [], emplacements: [],
      recherche: '', chargement: true, modal: false, erreur: '',
      form: {
        id: null, fournisseurId: '', entrepotId: '', zoneId: null,
        rayonId: null, etagereId: null, emplacementId: null,
        commentaire: '', controleQualiteOk: false, lignes: []
      },
      confirm: { visible: false, cible: null },
      toast: { visible: false, message: '', duree: 10000, _timer: null }
    }
  },
  computed: {
    peutEcrire()    { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE', 'MAGASINIER') },
    peutSupprimer() { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    zonesEntrepot() {
      if (!this.form.entrepotId) return this.zones
      return this.zones.filter(z => z.entrepotId === this.form.entrepotId)
    },
    rayonsZone() {
      return this.rayons.filter(r => r.zoneId === this.form.zoneId)
    },
    etagereRayon() {
      return this.etageres.filter(e => e.rayonId === this.form.rayonId)
    },
    emplacementsEtagere() {
      return this.emplacements.filter(e => e.etagereId === this.form.etagereId && e.actif)
    },
    emplacementSelectionne() {
      return this.emplacements.find(e => e.id === this.form.emplacementId)
    },
    cheminEmplacement() {
      if (!this.form.emplacementId) return ''
      const emp = this.emplacementSelectionne
      return emp ? emp.codeComplet : ''
    },
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(b =>
        b.fournisseurNom?.toLowerCase().includes(q) ||
        b.entrepotNom?.toLowerCase().includes(q) ||
        b.zoneNom?.toLowerCase().includes(q) ||
        b.emplacementCodeComplet?.toLowerCase().includes(q) ||
        b.statut?.toLowerCase().includes(q)
      )
    }
  },
  async mounted() {
    await Promise.all([this.charger(), this.chargerDonnees()])
  },
  beforeUnmount() {
    clearTimeout(this.toast._timer)
  },
  methods: {
    afficherToast(message, duree = 10000) {
      clearTimeout(this.toast._timer)
      this.toast = { visible: true, message, duree, _timer: null }
      this.toast._timer = setTimeout(() => { this.toast.visible = false }, duree)
    },
    fermerToast() {
      clearTimeout(this.toast._timer)
      this.toast.visible = false
    },
    async charger() {
      this.chargement = true
      try { const res = await receptionApi.findAll(); this.liste = res.data } finally { this.chargement = false }
    },
    async chargerDonnees() {
      const [f, e, z, p, r, et] = await Promise.all([
        fournisseurApi.findAll(), entrepotApi.findAll(), zoneApi.findAll(),
        produitApi.findAll(), rayonApi.findAll(), etagereApi.findAll()
      ])
      this.fournisseurs = f.data
      this.entrepots    = e.data
      this.zones        = z.data
      this.produits     = p.data
      this.rayons       = r.data
      this.etageres     = et.data
      // Emplacements chargés à la demande quand une étagère est sélectionnée
      const empRes = await emplacementApi.findAll()
      this.emplacements = empRes.data
    },
    ouvrirModal(b = null) {
      this.erreur = ''
      if (b) {
        this.form = {
          id:               b.id,
          fournisseurId:    b.fournisseurId,
          entrepotId:       b.entrepotId,
          zoneId:           b.zoneId           || null,
          rayonId:          b.rayonId           || null,
          etagereId:        b.etagereId         || null,
          emplacementId:    b.emplacementId     || null,
          commentaire:      b.commentaire       || '',
          controleQualiteOk: b.controleQualiteOk || false,
          lignes: (b.lignes || []).map(l => ({
            produitId:  l.produitId,
            quantite:   l.quantite,
            prixAchat:  l.prixAchat || 0
          }))
        }
      } else {
        this.form = {
          id: null, fournisseurId: '', entrepotId: '',
          zoneId: null, rayonId: null, etagereId: null, emplacementId: null,
          commentaire: '', controleQualiteOk: false, lignes: []
        }
      }
      this.modal = true
    },
    ajouterLigne() { this.form.lignes.push({ produitId: '', quantite: 1, prixAchat: 0 }) },
    supprimerLigne(index) { this.form.lignes.splice(index, 1) },
    async sauvegarder() {
      this.erreur = ''
      try {
        if (!this.form.fournisseurId) throw new Error('Le fournisseur est requis.')
        if (!this.form.entrepotId) throw new Error("L'entrepôt est requis.")
        if (this.form.lignes.length === 0) throw new Error('Ajoutez au moins une ligne de réception.')
        if (this.form.lignes.some(l => !l.produitId || l.quantite < 1))
          throw new Error('Chaque ligne doit contenir un produit et une quantité valide.')
        if (this.form.id) await receptionApi.modifier(this.form.id, this.form)
        else await receptionApi.creer(this.form)
        this.modal = false
        await this.charger()
      } catch (e) {
        this.erreur = e.response?.data?.message || e.message || 'Impossible de sauvegarder le bon.'
      }
    },
    async valider(bon) {
      try {
        await receptionApi.valider(bon.id)
        await this.charger()
      } catch (e) {
        const msg = e.response?.data?.message || 'Impossible de valider le bon.'
        this.afficherToast(msg)
      }
    },
    demanderSuppression(bon) { this.confirm = { visible: true, cible: bon } },
    async confirmerSuppression() {
      try {
        await receptionApi.supprimer(this.confirm.cible.id)
        this.confirm = { visible: false, cible: null }
        await this.charger()
      } catch (e) {
        this.confirm = { visible: false, cible: null }
        this.afficherToast(e.response?.data?.message || 'Impossible de supprimer le bon.')
      }
    },
    formaterDate(date) {
      if (!date) return '—'
      const d = new Date(date)
      return isNaN(d) ? '—' : d.toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    }
  }
}
</script>

<style scoped>
.empty-state { text-align:center;padding:32px;color:var(--gray-400); }
.large-modal { max-width: 900px; }
.line-table { margin-top: 20px; border: 1px solid var(--gray-200); border-radius: var(--radius-sm); padding: 18px; background: var(--gray-50); }
.line-header { font-weight: 700; margin-bottom: 14px; color: var(--gray-700); }
.line-row { margin-bottom: 14px; padding-bottom: 14px; border-bottom: 1px solid var(--gray-200); }
.line-row:last-child { margin-bottom: 0; border-bottom: none; }
.align-end { align-items: flex-end; }
.form-hint { font-weight:400; color:var(--gray-400); font-size:.74rem; margin-left:4px; }
.ref-code { font-size:.78rem;background:var(--navy-xlight);color:var(--navy);padding:2px 7px;border-radius:4px;font-family:monospace;font-weight:600; }
/* Récap emplacement sélectionné */
.emplacement-recap {
  display: flex; align-items: center; gap: 8px;
  background: linear-gradient(135deg, #eff6ff, #f0fdf4);
  border: 1px solid #93c5fd;
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  margin-bottom: 14px;
  font-size: .84rem; color: var(--navy);
}

/* ── Toast erreur ── */
.toast-error {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fff;
  border: 1px solid #fca5a5;
  border-left: 4px solid var(--danger);
  border-radius: var(--radius);
  box-shadow: var(--shadow-lg);
  padding: 16px 14px 12px 16px;
  max-width: 420px;
  min-width: 300px;
}
.toast-icon { color: var(--danger); flex-shrink: 0; margin-top: 1px; }
.toast-body { flex: 1; min-width: 0; }
.toast-title { font-weight: 700; font-size: .88rem; color: var(--danger); margin-bottom: 3px; }
.toast-message { font-size: .83rem; color: var(--gray-700); line-height: 1.4; }
.toast-close {
  background: none; border: none; cursor: pointer;
  color: var(--gray-400); padding: 2px; flex-shrink: 0;
  border-radius: 4px; display:flex; align-items:center;
}
.toast-close:hover { color: var(--gray-700); background: var(--gray-100); }

/* Barre de progression qui se vide */
.toast-progress { margin-top: 8px; height: 3px; background: var(--danger-bg); border-radius: 999px; overflow: hidden; }
.toast-progress-bar {
  height: 100%;
  background: var(--danger);
  border-radius: 999px;
  width: 100%;
  animation: toast-drain linear forwards;
}
@keyframes toast-drain { from { width: 100%; } to { width: 0%; } }

/* Transition d'entrée/sortie */
.toast-slide-enter-active { transition: all .25s ease; }
.toast-slide-leave-active { transition: all .2s ease; }
.toast-slide-enter-from { opacity: 0; transform: translateX(40px); }
.toast-slide-leave-to   { opacity: 0; transform: translateX(40px); }
</style>
