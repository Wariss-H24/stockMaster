<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Inventaires physiques</h1>
      <p class="page-subtitle">Module 11 — Comptage et ajustement des stocks</p>
    </div>

    <!-- Stats -->
    <div class="stats-grid" style="grid-template-columns:repeat(4,1fr);margin-bottom:24px;">
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--navy-xlight);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" stroke="var(--navy)" stroke-width="1.8"/><path d="M8 12h8M8 8h8M8 16h5" stroke="var(--navy)" stroke-width="1.6" stroke-linecap="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Total inventaires</p><p class="stat-value">{{ stats.total }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--warning-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="var(--warning)" stroke-width="1.8"/><path d="M12 7v5l3 3" stroke="var(--warning)" stroke-width="1.8" stroke-linecap="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">En cours</p><p class="stat-value">{{ stats.enCours }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--success-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M20 6L9 17l-5-5" stroke="var(--success)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Clôturés</p><p class="stat-value">{{ stats.clotures }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--danger-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M12 9v4M12 17h.01" stroke="var(--danger)" stroke-width="2" stroke-linecap="round"/><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="var(--danger)" stroke-width="1.8"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Lignes avec écart</p><p class="stat-value">{{ stats.ecarts }}</p></div>
      </div>
    </div>

    <!-- Tableau principal -->
    <div class="card" style="margin-bottom:24px;">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/><path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          <input v-model="recherche" placeholder="Référence, entrepôt, responsable..." />
        </div>
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirCreation">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
          Nouvel inventaire
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Référence</th>
              <th>Type</th>
              <th>Entrepôt / Zone</th>
              <th>Statut</th>
              <th>Responsable</th>
              <th>Date début</th>
              <th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement"><td colspan="7" class="empty-state">Chargement...</td></tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="7" class="empty-state">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" style="margin-bottom:8px;opacity:.3;display:block;margin-left:auto;margin-right:auto;"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M8 12h8M8 8h8M8 16h5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                Aucun inventaire trouvé.
              </td>
            </tr>
            <tr v-for="inv in listeFiltree" :key="inv.id">
              <td><code class="ref-code">{{ inv.reference }}</code></td>
              <td><span class="badge" :class="inv.type === 'COMPLET' ? 'badge-navy' : 'badge-info'">{{ inv.type }}</span></td>
              <td>
                <span>{{ inv.entrepotNom || '—' }}</span>
                <span v-if="inv.zoneNom" class="text-muted"> / {{ inv.zoneNom }}</span>
              </td>
              <td><span class="badge" :class="badgeStatut(inv.statut)">{{ inv.statut }}</span></td>
              <td>{{ inv.responsableNom || '—' }}</td>
              <td>{{ formaterDate(inv.dateDebut) }}</td>
              <td style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap;">
                  <button v-if="inv.statut === 'EN_COURS'" class="btn btn-primary btn-sm" @click="ouvrirSaisie(inv)">Saisir</button>
                  <button v-if="inv.statut === 'EN_COURS'" class="btn btn-outline btn-sm" @click="demanderAction(inv, 'cloturer')">Clôturer</button>
                  <button v-if="inv.statut === 'EN_COURS'" class="btn btn-danger btn-sm" @click="demanderAction(inv, 'annuler')">Annuler</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal : Création inventaire -->
    <div class="modal-overlay" v-if="modalCreation" @click.self="modalCreation = false">
      <div class="modal" style="max-width:520px;">
        <div class="modal-header">
          <h3 class="modal-title">Nouvel inventaire</h3>
          <button class="modal-close" @click="modalCreation = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Type *</label>
              <select class="form-select" v-model="form.type">
                <option value="COMPLET">Complet</option>
                <option value="PARTIEL">Partiel</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Entrepôt *</label>
              <select class="form-select" v-model.number="form.entrepotId" @change="form.zoneId = null">
                <option value="">Sélectionner</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Zone <span class="text-muted">(optionnel)</span></label>
            <select class="form-select" v-model.number="form.zoneId" :disabled="!form.entrepotId">
              <option :value="null">Toutes les zones</option>
              <option v-for="z in zonesFiltrees" :key="z.id" :value="z.id">{{ z.nom }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Commentaire</label>
            <textarea class="form-input" v-model="form.commentaire" rows="3" placeholder="Observations, contexte..."></textarea>
          </div>
          <div class="form-error" v-if="erreurModal">{{ erreurModal }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modalCreation = false">Annuler</button>
          <button class="btn btn-primary" @click="creer" :disabled="chargementAction">
            {{ chargementAction ? 'Création...' : 'Créer l\'inventaire' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal : Saisie lignes -->
    <div class="modal-overlay" v-if="modalSaisie" @click.self="modalSaisie = false">
      <div class="modal" style="max-width:900px;width:95vw;">
        <div class="modal-header">
          <h3 class="modal-title">Saisie des lignes — {{ inventaireSelectionne?.reference }}</h3>
          <button class="modal-close" @click="modalSaisie = false">✕</button>
        </div>
        <div class="modal-body" style="padding:0;">
          <div style="overflow-x:auto;">
            <table style="min-width:750px;">
              <thead>
                <tr>
                  <th>Produit</th>
                  <th>Référence</th>
                  <th>Emplacement</th>
                  <th>Stock système</th>
                  <th>Stock physique</th>
                  <th>Écart</th>
                  <th>Commentaire</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!lignesSaisie.length">
                  <td colspan="7" class="empty-state">Aucune ligne d'inventaire.</td>
                </tr>
                <tr v-for="(ligne, idx) in lignesSaisie" :key="idx">
                  <td>{{ ligne.produitNom }}</td>
                  <td><code class="ref-code">{{ ligne.produitReference }}</code></td>
                  <td>
                    <span v-if="ligne.emplacementCodeComplet" class="emp-code-inv" :title="ligne.emplacementCodeComplet">
                      {{ ligne.emplacementCodeComplet.split('/').pop() }}
                    </span>
                    <span v-else style="color:var(--gray-300);font-size:.78rem;">—</span>
                  </td>
                  <td style="text-align:right;font-weight:600;">{{ ligne.quantiteSysteme }}</td>
                  <td style="text-align:right;">
                    <input type="number" min="0" class="form-input" style="width:90px;text-align:right;"
                      v-model.number="ligne.quantitePhysique" />
                  </td>
                  <td style="text-align:right;font-weight:700;" :class="ecartClass(ligne)">
                    {{ ecart(ligne) >= 0 ? '+' : '' }}{{ ecart(ligne) }}
                  </td>
                  <td>
                    <input type="text" class="form-input" style="min-width:130px;" v-model="ligne.commentaire" placeholder="Note..." />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <!-- Résumé -->
          <div class="saisie-resume">
            <span>{{ lignesSaisie.length }} ligne(s)</span>
            <span class="ecart-pos">{{ nbEcartsPos }} écart(s) positif(s)</span>
            <span class="ecart-neg">{{ nbEcartsNeg }} écart(s) négatif(s)</span>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modalSaisie = false">Fermer</button>
          <button class="btn btn-primary" @click="sauvegarderLignes" :disabled="chargementAction">
            {{ chargementAction ? 'Enregistrement...' : 'Enregistrer' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal : Confirm action -->
    <div class="modal-overlay" v-if="confirm.visible" @click.self="confirm.visible = false">
      <div class="modal" style="max-width:420px;">
        <div class="modal-header">
          <h3 class="modal-title">{{ confirm.titre }}</h3>
          <button class="modal-close" @click="confirm.visible = false">✕</button>
        </div>
        <div class="modal-body">
          <p style="color:var(--gray-700);font-size:.9rem;line-height:1.6;">{{ confirm.message }}</p>
          <div v-if="confirm.action === 'cloturer'" class="confirm-warning">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M12 9v4M12 17h.01" stroke="var(--warning)" stroke-width="2" stroke-linecap="round"/><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="var(--warning)" stroke-width="1.8"/></svg>
            Cette action ajustera automatiquement les stocks selon les écarts constatés.
          </div>
          <div v-if="confirm.cible" class="confirm-detail">
            <p>Référence : <strong>{{ confirm.cible.reference }}</strong></p>
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

    <!-- Toast -->
    <transition name="toast-slide">
      <div v-if="toast.visible" :class="['toast-notif', toast.type === 'succes' ? 'toast-succes' : 'toast-erreur']">
        <div class="toast-icon">
          <svg v-if="toast.type === 'succes'" width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
        </div>
        <div class="toast-body">
          <p class="toast-title">{{ toast.type === 'succes' ? 'Succès' : 'Erreur' }}</p>
          <p class="toast-message">{{ toast.message }}</p>
          <div class="toast-progress"><div class="toast-progress-bar" :style="{ animationDuration: toast.duree + 'ms' }"></div></div>
        </div>
        <button class="toast-close" @click="fermerToast">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        </button>
      </div>
    </transition>
  </div>
</template>

<script>
import { inventaireApi, entrepotApi, zoneApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'

export default {
  name: 'InventairesPage',
  data() {
    return {
      liste: [],
      entrepots: [],
      zones: [],
      recherche: '',
      chargement: true,
      chargementAction: false,
      erreurModal: '',
      modalCreation: false,
      modalSaisie: false,
      inventaireSelectionne: null,
      lignesSaisie: [],
      form: { type: 'COMPLET', entrepotId: null, zoneId: null, commentaire: '' },
      confirm: { visible: false, action: '', cible: null, titre: '', message: '', labelConfirmer: '' },
      toast: { visible: false, type: 'succes', message: '', duree: 5000, _timer: null }
    }
  },
  computed: {
    peutEcrire() { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    listeFiltree() {
      const q = this.recherche.toLowerCase().trim()
      if (!q) return this.liste
      return this.liste.filter(i =>
        i.reference?.toLowerCase().includes(q) ||
        i.entrepotNom?.toLowerCase().includes(q) ||
        i.zoneNom?.toLowerCase().includes(q) ||
        i.responsableNom?.toLowerCase().includes(q) ||
        i.statut?.toLowerCase().includes(q)
      )
    },
    zonesFiltrees() {
      return this.zones.filter(z => z.entrepotId === this.form.entrepotId)
    },
    stats() {
      return {
        total: this.liste.length,
        enCours: this.liste.filter(i => i.statut === 'EN_COURS').length,
        clotures: this.liste.filter(i => i.statut === 'CLOTURE').length,
        ecarts: this.liste.reduce((acc, i) => acc + (i.nbEcarts || 0), 0)
      }
    },
    nbEcartsPos() {
      return this.lignesSaisie.filter(l => this.ecart(l) > 0).length
    },
    nbEcartsNeg() {
      return this.lignesSaisie.filter(l => this.ecart(l) < 0).length
    }
  },
  async mounted() {
    await Promise.all([this.charger(), this.chargerDonnees()])
  },
  beforeUnmount() { clearTimeout(this.toast._timer) },
  methods: {
    async charger() {
      this.chargement = true
      try {
        const res = await inventaireApi.findAll()
        this.liste = res.data
      } catch { this.afficherToast('Impossible de charger les inventaires.', 'erreur') }
      finally { this.chargement = false }
    },
    async chargerDonnees() {
      const [e, z] = await Promise.all([entrepotApi.findAll(), zoneApi.findAll()])
      this.entrepots = e.data
      this.zones = z.data
    },
    ouvrirCreation() {
      this.erreurModal = ''
      this.form = { type: 'COMPLET', entrepotId: null, zoneId: null, commentaire: '' }
      this.modalCreation = true
    },
    async creer() {
      this.erreurModal = ''
      if (!this.form.entrepotId) { this.erreurModal = 'L\'entrepôt est requis.'; return }
      this.chargementAction = true
      try {
        await inventaireApi.creer(this.form)
        this.modalCreation = false
        await this.charger()
        this.afficherToast('Inventaire créé avec succès.', 'succes')
      } catch (e) {
        this.erreurModal = e.response?.data?.message || 'Impossible de créer l\'inventaire.'
      } finally { this.chargementAction = false }
    },
    async ouvrirSaisie(inv) {
      this.inventaireSelectionne = inv
      try {
        const res = await inventaireApi.findById(inv.id)
        this.lignesSaisie = (res.data.lignes || []).map(l => ({
          ...l,
          quantitePhysique: l.quantitePhysique ?? l.quantiteSysteme ?? 0
        }))
      } catch { this.lignesSaisie = [] }
      this.modalSaisie = true
    },
    async sauvegarderLignes() {
      this.chargementAction = true
      try {
        await inventaireApi.mettreAJourLignes(this.inventaireSelectionne.id, this.lignesSaisie)
        this.modalSaisie = false
        await this.charger()
        this.afficherToast('Lignes enregistrées avec succès.', 'succes')
      } catch (e) {
        this.afficherToast(e.response?.data?.message || 'Impossible d\'enregistrer les lignes.', 'erreur')
      } finally { this.chargementAction = false }
    },
    demanderAction(inv, action) {
      const configs = {
        cloturer: { titre: 'Clôturer l\'inventaire', message: 'Confirmer la clôture de cet inventaire ?', labelConfirmer: 'Clôturer' },
        annuler:  { titre: 'Annuler l\'inventaire',  message: 'Voulez-vous annuler cet inventaire ? Cette action est irréversible.', labelConfirmer: 'Annuler' }
      }
      this.confirm = { visible: true, action, cible: inv, ...configs[action] }
    },
    async executerAction() {
      const { action, cible } = this.confirm
      this.chargementAction = true
      try {
        if (action === 'cloturer') await inventaireApi.cloturer(cible.id)
        else if (action === 'annuler') await inventaireApi.annuler(cible.id)
        this.confirm.visible = false
        await this.charger()
        this.afficherToast(action === 'cloturer' ? 'Inventaire clôturé — stocks ajustés.' : 'Inventaire annulé.', 'succes')
      } catch (e) {
        this.confirm.visible = false
        this.afficherToast(e.response?.data?.message || 'L\'opération a échoué.', 'erreur')
      } finally { this.chargementAction = false }
    },
    ecart(ligne) { return (ligne.quantitePhysique ?? 0) - (ligne.quantiteSysteme ?? 0) },
    ecartClass(ligne) {
      const e = this.ecart(ligne)
      if (e > 0) return 'ecart-pos'
      if (e < 0) return 'ecart-neg'
      return 'ecart-zero'
    },
    badgeStatut(s) {
      return { EN_COURS: 'badge-warning', CLOTURE: 'badge-success', ANNULE: 'badge-danger' }[s] || 'badge-navy'
    },
    formaterDate(d) {
      if (!d) return '—'
      const dt = new Date(d)
      return isNaN(dt) ? '—' : dt.toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    },
    afficherToast(message, type = 'succes', duree = 5000) {
      clearTimeout(this.toast._timer)
      this.toast = { visible: true, type, message, duree, _timer: null }
      this.toast._timer = setTimeout(() => { this.toast.visible = false }, duree)
    },
    fermerToast() { clearTimeout(this.toast._timer); this.toast.visible = false }
  }
}
</script>

<style scoped>
.text-muted { color: var(--gray-400); font-size: .82rem; }
.empty-state { text-align: center; padding: 48px 20px; color: var(--gray-400); font-size: .9rem; }
.ref-code { font-size: .82rem; background: var(--gray-100); padding: 2px 6px; border-radius: 4px; font-family: monospace; }
.emp-code-inv {
  font-size: .76rem; font-family: monospace; font-weight: 600;
  background: var(--navy-xlight); color: var(--navy);
  padding: 2px 6px; border-radius: 4px;
}
.ecart-pos { color: var(--success); }
.ecart-neg { color: var(--danger); }
.ecart-zero { color: var(--gray-400); }
.saisie-resume {
  display: flex; gap: 24px; padding: 12px 20px;
  background: var(--gray-50); border-top: 1px solid var(--gray-200);
  font-size: .85rem; color: var(--gray-600); font-weight: 500;
}
.confirm-warning {
  display: flex; align-items: flex-start; gap: 8px;
  background: #fffbeb; border: 1px solid #fde68a; border-radius: var(--radius-sm);
  padding: 10px 12px; margin-top: 12px; font-size: .84rem; color: #92400e; line-height: 1.5;
}
.confirm-detail {
  margin-top: 12px; background: var(--gray-50); border: 1px solid var(--gray-200);
  border-radius: var(--radius-sm); padding: 10px 14px; font-size: .82rem; color: var(--gray-600);
}
/* Toast */
.toast-notif { position:fixed;top:24px;right:24px;z-index:9999;display:flex;align-items:flex-start;gap:12px;border-radius:var(--radius);box-shadow:var(--shadow-lg);padding:16px 14px 12px 16px;max-width:420px;min-width:300px; }
.toast-succes { background:#f0fdf4;border:1px solid #86efac;border-left:4px solid var(--success); }
.toast-succes .toast-icon,.toast-succes .toast-title { color:var(--success); }
.toast-succes .toast-progress-bar { background:var(--success); }
.toast-succes .toast-progress { background:rgba(22,163,74,.12); }
.toast-erreur { background:#fff5f5;border:1px solid #fca5a5;border-left:4px solid var(--danger); }
.toast-erreur .toast-icon,.toast-erreur .toast-title { color:var(--danger); }
.toast-erreur .toast-progress-bar { background:var(--danger); }
.toast-erreur .toast-progress { background:rgba(220,38,38,.12); }
.toast-icon { flex-shrink:0;margin-top:1px; }
.toast-body { flex:1;min-width:0; }
.toast-title { font-weight:700;font-size:.88rem;margin-bottom:3px; }
.toast-message { font-size:.83rem;color:var(--gray-700);line-height:1.4; }
.toast-progress { margin-top:8px;height:3px;border-radius:999px;overflow:hidden; }
.toast-progress-bar { width:100%;height:100%;border-radius:999px;animation:toast-drain linear forwards; }
.toast-close { background:none;border:none;cursor:pointer;color:var(--gray-400);padding:2px;flex-shrink:0;border-radius:4px;display:flex;align-items:center; }
.toast-close:hover { color:var(--gray-700);background:var(--gray-100); }
@keyframes toast-drain { from { width:100%; } to { width:0%; } }
.toast-slide-enter-active { transition:all .25s ease; }
.toast-slide-leave-active { transition:all .2s ease; }
.toast-slide-enter-from,.toast-slide-leave-to { opacity:0;transform:translateX(40px); }
</style>
