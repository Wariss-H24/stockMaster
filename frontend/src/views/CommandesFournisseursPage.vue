<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Commandes Fournisseurs</h1>
      <p class="page-subtitle">Module 16 — Gestion du cycle d'approvisionnement</p>
    </div>

    <!-- Stats -->
    <div class="stats-grid" style="grid-template-columns:repeat(4,1fr);margin-bottom:24px;">
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--navy-xlight);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4zM3 6h18M16 10a4 4 0 01-8 0" stroke="var(--navy)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Total commandes</p><p class="stat-value">{{ liste.length }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--warning-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M12 20h9M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z" stroke="var(--warning)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Brouillons</p><p class="stat-value">{{ stats.brouillons }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--info-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="var(--info)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Envoyées</p><p class="stat-value">{{ stats.envoyees }}</p></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--success-bg);">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M20 6L9 17l-5-5" stroke="var(--success)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <div class="stat-body"><p class="stat-label">Reçues</p><p class="stat-value">{{ stats.recues }}</p></div>
      </div>
    </div>

    <!-- Tableau -->
    <div class="card" style="margin-bottom:24px;">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/><path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          <input v-model="recherche" placeholder="Référence, fournisseur, entrepôt..." />
        </div>
        <button class="btn btn-primary" @click="ouvrirModal()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
          Nouvelle commande
        </button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Référence</th>
              <th>Fournisseur</th>
              <th>Entrepôt</th>
              <th style="text-align:right;">Nb lignes</th>
              <th style="text-align:right;">Montant total</th>
              <th>Statut</th>
              <th>Date création</th>
              <th>Livraison souhaitée</th>
              <th style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement"><td colspan="9" class="empty-state">Chargement...</td></tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="9" class="empty-state">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" style="display:block;margin:0 auto 8px;opacity:.3;"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4zM3 6h18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                Aucune commande trouvée.
              </td>
            </tr>
            <tr v-for="cmd in listeFiltree" :key="cmd.id">
              <td><code class="ref-code">{{ cmd.reference }}</code></td>
              <td>{{ cmd.fournisseurNom }}</td>
              <td>{{ cmd.entrepotNom }}</td>
              <td style="text-align:right;">{{ (cmd.lignes || []).length }}</td>
              <td style="text-align:right;font-weight:600;">{{ formaterMontant(cmd.montantTotal) }}</td>
              <td><span class="badge" :class="badgeStatut(cmd.statut)">{{ cmd.statut }}</span></td>
              <td>{{ formaterDate(cmd.dateCrea) }}</td>
              <td>{{ formaterDate(cmd.dateLivraisonSouhaitee) }}</td>
              <td style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;flex-wrap:wrap;">
                  <button v-if="cmd.statut === 'BROUILLON'" class="btn btn-outline btn-sm" @click="ouvrirModal(cmd)">Voir/Éditer</button>
                  <button v-if="cmd.statut === 'BROUILLON'" class="btn btn-primary btn-sm" @click="demanderAction(cmd, 'envoyer')">Envoyer</button>
                  <button v-if="cmd.statut === 'BROUILLON'" class="btn btn-danger btn-sm" @click="demanderAction(cmd, 'supprimer')">Supprimer</button>
                  <button v-if="cmd.statut === 'ENVOYEE'" class="btn btn-primary btn-sm" @click="demanderAction(cmd, 'receptionner')">Réceptionner</button>
                  <button v-if="cmd.statut === 'ENVOYEE'" class="btn btn-danger btn-sm" @click="demanderAction(cmd, 'annuler')">Annuler</button>
                  <button v-if="cmd.statut === 'RECUE' || cmd.statut === 'ANNULEE'" class="btn btn-outline btn-sm" @click="ouvrirModalVoir(cmd)">Voir</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal création / édition -->
    <div class="modal-overlay" v-if="modal" @click.self="modal = false">
      <div class="modal" style="max-width:900px;width:95vw;">
        <div class="modal-header">
          <h3 class="modal-title">{{ modeEdition ? (lectureSeule ? 'Détail commande' : 'Modifier la commande') : 'Nouvelle commande' }}</h3>
          <button class="modal-close" @click="modal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Fournisseur *</label>
              <select class="form-select" v-model="form.fournisseurId" :disabled="lectureSeule">
                <option value="">Sélectionner</option>
                <option v-for="f in fournisseurs" :key="f.id" :value="f.id">{{ f.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Entrepôt *</label>
              <select class="form-select" v-model="form.entrepotId" :disabled="lectureSeule">
                <option value="">Sélectionner</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Date livraison souhaitée</label>
              <input type="date" class="form-input" v-model="form.dateLivraisonSouhaitee" :disabled="lectureSeule" />
            </div>
            <div class="form-group">
              <label class="form-label">Commentaire</label>
              <input type="text" class="form-input" v-model="form.commentaire" :disabled="lectureSeule" placeholder="Observations..." />
            </div>
          </div>

          <!-- Lignes -->
          <div style="margin-top:16px;">
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px;">
              <h4 style="font-size:.9rem;font-weight:700;color:var(--gray-800);">Lignes de commande</h4>
              <button v-if="!lectureSeule" class="btn btn-outline btn-sm" @click="ajouterLigne">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none"><path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
                Ajouter une ligne
              </button>
            </div>
            <div style="overflow-x:auto;">
              <table style="min-width:600px;">
                <thead>
                  <tr>
                    <th>Produit *</th>
                    <th style="width:110px;">Quantité *</th>
                    <th style="width:130px;">Prix unitaire (€)</th>
                    <th style="width:120px;text-align:right;">Sous-total</th>
                    <th v-if="!lectureSeule" style="width:50px;"></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="form.lignes.length === 0">
                    <td :colspan="lectureSeule ? 4 : 5" class="empty-state" style="padding:20px;">Aucune ligne — cliquez "Ajouter une ligne"</td>
                  </tr>
                  <tr v-for="(ligne, idx) in form.lignes" :key="idx">
                    <td>
                      <select class="form-select" v-model="ligne.produitId" :disabled="lectureSeule">
                        <option value="">Sélectionner</option>
                        <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.nom }}</option>
                      </select>
                    </td>
                    <td><input type="number" min="1" class="form-input" v-model.number="ligne.quantite" :disabled="lectureSeule" /></td>
                    <td><input type="number" min="0" step="0.01" class="form-input" v-model.number="ligne.prixUnitaire" :disabled="lectureSeule" /></td>
                    <td style="text-align:right;font-weight:600;">{{ formaterMontant((ligne.quantite || 0) * (ligne.prixUnitaire || 0)) }}</td>
                    <td v-if="!lectureSeule" style="text-align:center;">
                      <button class="btn btn-danger btn-sm" style="padding:4px 8px;" @click="supprimerLigne(idx)">✕</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <!-- Total -->
            <div class="cmd-total">
              <span>Total commande :</span>
              <strong style="font-size:1.05rem;color:var(--navy);">{{ formaterMontant(montantTotal) }}</strong>
            </div>
          </div>

          <div class="form-error" v-if="erreurModal">{{ erreurModal }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modal = false">{{ lectureSeule ? 'Fermer' : 'Annuler' }}</button>
          <button v-if="!lectureSeule" class="btn btn-primary" @click="sauvegarder" :disabled="chargementAction">
            {{ chargementAction ? 'Sauvegarde...' : (modeEdition ? 'Mettre à jour' : 'Créer en brouillon') }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modal confirm action -->
    <div class="modal-overlay" v-if="confirm.visible" @click.self="confirm.visible = false">
      <div class="modal" style="max-width:420px;">
        <div class="modal-header">
          <h3 class="modal-title">{{ confirm.titre }}</h3>
          <button class="modal-close" @click="confirm.visible = false">✕</button>
        </div>
        <div class="modal-body">
          <p style="color:var(--gray-700);font-size:.9rem;line-height:1.6;">{{ confirm.message }}</p>
          <div v-if="confirm.action === 'receptionner'" class="confirm-warning">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M12 9v4M12 17h.01" stroke="var(--warning)" stroke-width="2" stroke-linecap="round"/><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" stroke="var(--warning)" stroke-width="1.8"/></svg>
            Les stocks de l'entrepôt seront mis à jour selon les quantités commandées.
          </div>
          <div v-if="confirm.cible" class="confirm-detail">
            <p>Référence : <strong>{{ confirm.cible.reference }}</strong></p>
            <p style="margin-top:4px;">Fournisseur : <strong>{{ confirm.cible.fournisseurNom }}</strong></p>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="confirm.visible = false">Annuler</button>
          <button :class="['btn', confirm.action === 'annuler' || confirm.action === 'supprimer' ? 'btn-danger' : 'btn-primary']"
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
import { commandeApi, fournisseurApi, entrepotApi, produitApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'

const formVide = () => ({
  fournisseurId: null, entrepotId: null,
  dateLivraisonSouhaitee: '', commentaire: '',
  lignes: []
})
const ligneVide = () => ({ produitId: null, quantite: 1, prixUnitaire: 0 })

export default {
  name: 'CommandesFournisseursPage',
  data() {
    return {
      liste: [], fournisseurs: [], entrepots: [], produits: [],
      recherche: '', chargement: true, chargementAction: false,
      erreurModal: '', modal: false, modeEdition: false, lectureSeule: false,
      form: formVide(),
      commandeEditId: null,
      confirm: { visible: false, action: '', cible: null, titre: '', message: '', labelConfirmer: '' },
      toast: { visible: false, type: 'succes', message: '', duree: 5000, _timer: null }
    }
  },
  computed: {
    listeFiltree() {
      const q = this.recherche.toLowerCase().trim()
      if (!q) return this.liste
      return this.liste.filter(c =>
        c.reference?.toLowerCase().includes(q) ||
        c.fournisseurNom?.toLowerCase().includes(q) ||
        c.entrepotNom?.toLowerCase().includes(q) ||
        c.statut?.toLowerCase().includes(q)
      )
    },
    montantTotal() {
      return this.form.lignes.reduce((s, l) => s + (l.quantite || 0) * (l.prixUnitaire || 0), 0)
    },
    stats() {
      return {
        brouillons: this.liste.filter(c => c.statut === 'BROUILLON').length,
        envoyees:   this.liste.filter(c => c.statut === 'ENVOYEE').length,
        recues:     this.liste.filter(c => c.statut === 'RECUE').length
      }
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
        const res = await commandeApi.findAll()
        this.liste = res.data
      } catch { this.afficherToast('Impossible de charger les commandes.', 'erreur') }
      finally { this.chargement = false }
    },
    async chargerDonnees() {
      const [f, e, p] = await Promise.all([fournisseurApi.findAll(), entrepotApi.findAll(), produitApi.findAll()])
      this.fournisseurs = f.data
      this.entrepots    = e.data
      this.produits     = p.data
    },
    ouvrirModal(cmd = null) {
      this.erreurModal = ''
      this.lectureSeule = false
      if (cmd) {
        this.modeEdition = true
        this.commandeEditId = cmd.id
        this.form = {
          fournisseurId: cmd.fournisseurId,
          entrepotId: cmd.entrepotId,
          dateLivraisonSouhaitee: cmd.dateLivraisonSouhaitee ? cmd.dateLivraisonSouhaitee.substring(0, 10) : '',
          commentaire: cmd.commentaire || '',
          // Ne garder que les champs nécessaires — évite les champs parasites (sousTotal, produitNom...)
          lignes: (cmd.lignes || []).map(l => ({
            produitId:    l.produitId,
            quantite:     l.quantite     || 1,
            prixUnitaire: l.prixUnitaire || 0
          }))
        }
      } else {
        this.modeEdition = false
        this.commandeEditId = null
        this.form = formVide()
      }
      this.modal = true
    },
    ouvrirModalVoir(cmd) {
      this.erreurModal = ''
      this.lectureSeule = true
      this.modeEdition = true
      this.commandeEditId = cmd.id
      this.form = {
        fournisseurId: cmd.fournisseurId,
        entrepotId: cmd.entrepotId,
        dateLivraisonSouhaitee: cmd.dateLivraisonSouhaitee ? cmd.dateLivraisonSouhaitee.substring(0, 10) : '',
        commentaire: cmd.commentaire || '',
        lignes: (cmd.lignes || []).map(l => ({
          produitId:    l.produitId,
          quantite:     l.quantite     || 1,
          prixUnitaire: l.prixUnitaire || 0
        }))
      }
      this.modal = true
    },
    async sauvegarder() {
      this.erreurModal = ''

      // Guard mode édition
      if (this.modeEdition && !this.commandeEditId) {
        this.erreurModal = 'ID de commande manquant. Fermez et réouvrez la commande.'
        return
      }

      if (!this.form.fournisseurId)  { this.erreurModal = 'Le fournisseur est requis.'; return }
      if (!this.form.entrepotId)     { this.erreurModal = 'L\'entrepôt est requis.'; return }
      if (!this.form.lignes.length)  { this.erreurModal = 'Ajoutez au moins une ligne de produit.'; return }

      // Vérifier chaque ligne
      for (let i = 0; i < this.form.lignes.length; i++) {
        const l = this.form.lignes[i]
        if (!l.produitId || l.produitId === '' || l.produitId === 0) {
          this.erreurModal = `Ligne ${i + 1} : sélectionnez un produit.`
          return
        }
        if (!l.quantite || l.quantite < 1) {
          this.erreurModal = `Ligne ${i + 1} : la quantité doit être au moins 1.`
          return
        }
      }

      // Construire le payload propre — conversion explicite en entiers
      const toInt = v => { const n = parseInt(v, 10); return isNaN(n) ? null : n }
      const toFloat = v => { const n = parseFloat(v); return isNaN(n) ? 0 : n }

      const payload = {
        fournisseurId: toInt(this.form.fournisseurId),
        entrepotId:    toInt(this.form.entrepotId),
        dateLivraisonSouhaitee: this.form.dateLivraisonSouhaitee || null,
        commentaire:   this.form.commentaire || '',
        lignes: this.form.lignes.map(l => ({
          produitId:    toInt(l.produitId),
          quantite:     toInt(l.quantite) || 1,
          prixUnitaire: toFloat(l.prixUnitaire)
        }))
      }

      // Vérification finale après conversion
      if (!payload.fournisseurId) { this.erreurModal = 'Fournisseur invalide. Veuillez sélectionner un fournisseur.'; return }
      if (!payload.entrepotId)    { this.erreurModal = 'Entrepôt invalide. Veuillez sélectionner un entrepôt.'; return }
      for (let i = 0; i < payload.lignes.length; i++) {
        if (!payload.lignes[i].produitId || payload.lignes[i].produitId <= 0) {
          this.erreurModal = `Ligne ${i + 1} : veuillez sélectionner un produit.`
          return
        }
      }

      this.chargementAction = true
      try {
        if (this.modeEdition) {
          await commandeApi.modifier(this.commandeEditId, payload)
          this.afficherToast('Commande mise à jour avec succès.', 'succes')
        } else {
          await commandeApi.creer(payload)
          this.afficherToast('Commande créée en brouillon.', 'succes')
        }
        this.modal = false
        await this.charger()
      } catch (e) {
        const data = e.response?.data
        if (data?.erreurs) {
          this.erreurModal = Object.values(data.erreurs).join(' — ')
        } else if (data?.message) {
          this.erreurModal = data.message
        } else {
          this.erreurModal = 'Une erreur est survenue. Vérifiez les champs et réessayez.'
        }
      } finally { this.chargementAction = false }
    },
    demanderAction(cmd, action) {
      const configs = {
        envoyer:      { titre: 'Envoyer la commande',   message: 'Confirmer l\'envoi de cette commande au fournisseur ?', labelConfirmer: 'Envoyer' },
        receptionner: { titre: 'Réceptionner la commande', message: 'Confirmer la réception de cette commande ?', labelConfirmer: 'Réceptionner' },
        annuler:      { titre: 'Annuler la commande',   message: 'Voulez-vous annuler cette commande ? Action irréversible.', labelConfirmer: 'Annuler' },
        supprimer:    { titre: 'Supprimer la commande', message: 'Voulez-vous supprimer définitivement ce brouillon ?', labelConfirmer: 'Supprimer' }
      }
      this.confirm = { visible: true, action, cible: cmd, ...configs[action] }
    },
    async executerAction() {
      const { action, cible } = this.confirm
      this.chargementAction = true
      try {
        if      (action === 'envoyer')      await commandeApi.envoyer(cible.id)
        else if (action === 'receptionner') await commandeApi.receptionner(cible.id)
        else if (action === 'annuler')      await commandeApi.annuler(cible.id)
        else if (action === 'supprimer')    await commandeApi.supprimer(cible.id)
        this.confirm.visible = false
        await this.charger()
        const msgs = {
          envoyer: 'Commande envoyée au fournisseur.',
          receptionner: 'Bon de réception créé avec succès.',
          annuler: 'Commande annulée.',
          supprimer: 'Commande supprimée.'
        }
        this.afficherToast(msgs[action], 'succes')
      } catch (e) {
        this.confirm.visible = false
        this.afficherToast(e.response?.data?.message || 'L\'opération a échoué.', 'erreur')
      } finally { this.chargementAction = false }
    },
    ajouterLigne() { this.form.lignes.push(ligneVide()) },
    supprimerLigne(idx) { this.form.lignes.splice(idx, 1) },
    badgeStatut(s) {
      return { BROUILLON: 'badge-warning', ENVOYEE: 'badge-info', RECUE: 'badge-success', ANNULEE: 'badge-danger' }[s] || 'badge-navy'
    },
    peutEcrire() { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    formaterDate(d) {
      if (!d) return '—'
      const dt = new Date(d)
      if (isNaN(dt)) return '—'
      // LocalDate (YYYY-MM-DD) — pas d'heure
      if (typeof d === 'string' && d.length === 10) {
        return dt.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' })
      }
      return dt.toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    },
    formaterMontant(v) {
      return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(v || 0)
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
.empty-state { text-align:center;padding:48px 20px;color:var(--gray-400);font-size:.9rem; }
.ref-code { font-size:.82rem;background:var(--gray-100);padding:2px 6px;border-radius:4px;font-family:monospace; }
.cmd-total {
  display: flex; align-items: center; justify-content: flex-end; gap: 12px;
  padding: 12px 16px; background: var(--gray-50); border-top: 1px solid var(--gray-200);
  border-radius: 0 0 var(--radius-sm) var(--radius-sm); margin-top: 0;
  font-size: .9rem; color: var(--gray-700);
}
.confirm-warning {
  display:flex;align-items:flex-start;gap:8px;
  background:#fffbeb;border:1px solid #fde68a;border-radius:var(--radius-sm);
  padding:10px 12px;margin-top:12px;font-size:.84rem;color:#92400e;line-height:1.5;
}
.confirm-detail {
  margin-top:12px;background:var(--gray-50);border:1px solid var(--gray-200);
  border-radius:var(--radius-sm);padding:10px 14px;font-size:.82rem;color:var(--gray-600);
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
