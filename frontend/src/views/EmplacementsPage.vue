<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Gestion des emplacements</h1>
      <p class="page-subtitle">Module 17 — Entrepôt → Zone → Rayon → Étagère → Emplacement</p>
    </div>

    <!-- Breadcrumb navigation -->
    <div class="breadcrumb-nav">
      <button class="breadcrumb-item" :class="{ active: niveau === 'rayon' }" @click="allerNiveau('rayon')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
        Rayons
      </button>
      <span class="breadcrumb-sep">›</span>
      <button class="breadcrumb-item" :class="{ active: niveau === 'etagere' }"
        @click="allerNiveau('etagere')" :disabled="!rayonSelectionne">
        Étagères {{ rayonSelectionne ? '— ' + rayonSelectionne.nom : '' }}
      </button>
      <span class="breadcrumb-sep">›</span>
      <button class="breadcrumb-item" :class="{ active: niveau === 'emplacement' }"
        @click="allerNiveau('emplacement')" :disabled="!etagereSelectionnee">
        Emplacements {{ etagereSelectionnee ? '— ' + etagereSelectionnee.nom : '' }}
      </button>
    </div>

    <!-- Filtre entrepôt/zone -->
    <div class="card" style="margin-bottom:16px;padding:14px 20px;" v-if="niveau === 'rayon'">
      <div style="display:flex;gap:12px;flex-wrap:wrap;">
        <div class="form-group" style="margin:0;flex:1;min-width:180px;">
          <label class="form-label">Entrepôt</label>
          <select class="form-select" v-model.number="filtreEntrepotId" @change="filtreZoneId = null">
            <option :value="null">Tous les entrepôts</option>
            <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
          </select>
        </div>
        <div class="form-group" style="margin:0;flex:1;min-width:180px;">
          <label class="form-label">Zone</label>
          <select class="form-select" v-model.number="filtreZoneId" :disabled="!filtreEntrepotId">
            <option :value="null">Toutes les zones</option>
            <option v-for="z in zonesFiltrees" :key="z.id" :value="z.id">{{ z.nom }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- RAYONS -->
    <div v-if="niveau === 'rayon'" class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/><path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          <input v-model="recherche" placeholder="Rechercher un rayon..." />
        </div>
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirModal('rayon')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
          Nouveau rayon
        </button>
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr>
            <th>Code</th><th>Nom</th><th>Entrepôt</th><th>Zone</th>
            <th style="text-align:center;">Étagères</th><th>Statut</th>
            <th style="text-align:right;">Actions</th>
          </tr></thead>
          <tbody>
            <tr v-if="chargement"><td colspan="7" class="empty-state">Chargement...</td></tr>
            <tr v-else-if="listeFiltree.length === 0"><td colspan="7" class="empty-state">Aucun rayon. Créez-en un.</td></tr>
            <tr v-for="r in listeFiltree" :key="r.id" :class="{ 'row-inactive': !r.actif }">
              <td><code class="ref-code">{{ r.code }}</code></td>
              <td style="font-weight:600;">{{ r.nom }}</td>
              <td>{{ r.entrepotNom }}</td>
              <td><span class="badge badge-info">{{ r.zoneNom }}</span></td>
              <td style="text-align:center;"><span class="badge badge-navy">{{ r.nbEtageres }}</span></td>
              <td><span class="badge" :class="r.actif ? 'badge-success' : 'badge-danger'">{{ r.actif ? 'Actif' : 'Inactif' }}</span></td>
              <td style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <button class="btn btn-primary btn-sm" @click="selectionnerRayon(r)">Étagères →</button>
                  <button v-if="peutEcrire" class="btn btn-outline btn-sm" @click="ouvrirModal('rayon', r)">Éditer</button>
                  <button v-if="peutEcrire && r.actif" class="btn btn-danger btn-sm" @click="demanderDesactivation('rayon', r)">Désactiver</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ÉTAGÈRES -->
    <div v-if="niveau === 'etagere'" class="card">
      <div class="toolbar">
        <div style="display:flex;align-items:center;gap:8px;">
          <button class="btn btn-outline btn-sm" @click="allerNiveau('rayon')">← Retour</button>
          <span style="font-size:.84rem;color:var(--gray-500);">Rayon : <strong>{{ rayonSelectionne?.code }} — {{ rayonSelectionne?.nom }}</strong></span>
        </div>
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirModal('etagere')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
          Nouvelle étagère
        </button>
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr>
            <th>Code</th><th>Nom</th><th>Niveaux</th>
            <th style="text-align:center;">Emplacements</th><th>Statut</th>
            <th style="text-align:right;">Actions</th>
          </tr></thead>
          <tbody>
            <tr v-if="chargement"><td colspan="6" class="empty-state">Chargement...</td></tr>
            <tr v-else-if="etageres.length === 0"><td colspan="6" class="empty-state">Aucune étagère dans ce rayon.</td></tr>
            <tr v-for="e in etageres" :key="e.id" :class="{ 'row-inactive': !e.actif }">
              <td><code class="ref-code">{{ e.code }}</code></td>
              <td style="font-weight:600;">{{ e.nom }}</td>
              <td>{{ e.niveaux || '—' }}</td>
              <td style="text-align:center;"><span class="badge badge-navy">{{ e.nbEmplacements }}</span></td>
              <td><span class="badge" :class="e.actif ? 'badge-success' : 'badge-danger'">{{ e.actif ? 'Actif' : 'Inactif' }}</span></td>
              <td style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <button class="btn btn-primary btn-sm" @click="selectionnerEtagere(e)">Emplacements →</button>
                  <button v-if="peutEcrire" class="btn btn-outline btn-sm" @click="ouvrirModal('etagere', e)">Éditer</button>
                  <button v-if="peutEcrire && e.actif" class="btn btn-danger btn-sm" @click="demanderDesactivation('etagere', e)">Désactiver</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- EMPLACEMENTS -->
    <div v-if="niveau === 'emplacement'" class="card">
      <div class="toolbar">
        <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">
          <button class="btn btn-outline btn-sm" @click="allerNiveau('etagere')">← Retour</button>
          <span class="chemin">{{ etagereSelectionnee?.entrepotNom }} / {{ etagereSelectionnee?.zoneNom }} / {{ etagereSelectionnee?.rayonNom }} / <strong>{{ etagereSelectionnee?.nom }}</strong></span>
        </div>
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirModal('emplacement')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>
          Nouvel emplacement
        </button>
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr>
            <th>Code complet</th><th>Nom</th><th>Capacité</th>
            <th>Occupation</th><th>Statut</th>
            <th v-if="peutEcrire" style="text-align:right;">Actions</th>
          </tr></thead>
          <tbody>
            <tr v-if="chargement"><td colspan="6" class="empty-state">Chargement...</td></tr>
            <tr v-else-if="emplacements.length === 0"><td colspan="6" class="empty-state">Aucun emplacement dans cette étagère.</td></tr>
            <tr v-for="emp in emplacements" :key="emp.id" :class="{ 'row-inactive': !emp.actif }">
              <td><code class="ref-code-lg">{{ emp.codeComplet }}</code></td>
              <td style="font-weight:600;">{{ emp.nom }}</td>
              <td style="font-size:.84rem;color:var(--gray-600);">{{ emp.capaciteMax ? emp.capaciteUtilisee + ' / ' + emp.capaciteMax : '—' }}</td>
              <td>
                <div v-if="emp.capaciteMax" style="display:flex;align-items:center;gap:8px;">
                  <div class="progress-bar" style="width:80px;">
                    <div class="progress-fill"
                      :class="(emp.tauxOccupation||0) < 60 ? 'progress-low' : (emp.tauxOccupation||0) < 85 ? 'progress-mid' : 'progress-high'"
                      :style="{ width: (emp.tauxOccupation||0) + '%' }"></div>
                  </div>
                  <span style="font-size:.78rem;color:var(--gray-500);">{{ emp.tauxOccupation||0 }}%</span>
                </div>
                <span v-else style="color:var(--gray-300);font-size:.82rem;">sans limite</span>
              </td>
              <td><span class="badge" :class="emp.actif ? 'badge-success' : 'badge-danger'">{{ emp.actif ? 'Actif' : 'Inactif' }}</span></td>
              <td v-if="peutEcrire" style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <button class="btn btn-outline btn-sm" @click="ouvrirModal('emplacement', emp)">Éditer</button>
                  <button v-if="emp.actif" class="btn btn-danger btn-sm" @click="demanderDesactivation('emplacement', emp)">Désactiver</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- MODAL FORMULAIRE -->
    <div class="modal-overlay" v-if="modal.visible" @click.self="modal.visible = false">
      <div class="modal" style="max-width:520px;">
        <div class="modal-header">
          <h3 class="modal-title">{{ modal.titre }}</h3>
          <button class="modal-close" @click="modal.visible = false">✕</button>
        </div>
        <div class="modal-body">
          <!-- RAYON -->
          <template v-if="modal.type === 'rayon'">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Zone parente *</label>
                <select class="form-select" v-model.number="modal.form.zoneId">
                  <option value="">Sélectionner une zone</option>
                  <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.entrepotNom }} — {{ z.nom }}</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">Code <span style="color:var(--gray-400);font-size:.74rem;">(auto si vide)</span></label>
                <input class="form-input" v-model="modal.form.code" placeholder="RAYON-01" />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Nom *</label>
              <input class="form-input" v-model="modal.form.nom" placeholder="Rayon produits frais" />
            </div>
            <div class="form-group">
              <label class="form-label">Description</label>
              <input class="form-input" v-model="modal.form.description" placeholder="Description optionnelle" />
            </div>
          </template>
          <!-- ÉTAGÈRE -->
          <template v-if="modal.type === 'etagere'">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Rayon *</label>
                <select class="form-select" v-model.number="modal.form.rayonId" :disabled="!!rayonSelectionne">
                  <option value="">Sélectionner</option>
                  <option v-for="r in rayons" :key="r.id" :value="r.id">{{ r.code }} — {{ r.nom }}</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">Code</label>
                <input class="form-input" v-model="modal.form.code" placeholder="ETAGERE-01" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Nom *</label>
                <input class="form-input" v-model="modal.form.nom" placeholder="Étagère A" />
              </div>
              <div class="form-group">
                <label class="form-label">Niveaux</label>
                <input class="form-input" type="number" min="1" max="20" v-model.number="modal.form.niveaux" placeholder="4" />
              </div>
            </div>
          </template>
          <!-- EMPLACEMENT -->
          <template v-if="modal.type === 'emplacement'">
            <div class="form-group">
              <label class="form-label">Étagère parente</label>
              <div class="form-input" style="background:var(--gray-50);color:var(--gray-600);cursor:default;font-size:.84rem;">
                {{ etagereSelectionnee?.entrepotNom }} / {{ etagereSelectionnee?.zoneNom }} / {{ etagereSelectionnee?.rayonNom }} / {{ etagereSelectionnee?.nom }}
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Code unique *</label>
                <input class="form-input" v-model="modal.form.code" placeholder="EMP-001" />
              </div>
              <div class="form-group">
                <label class="form-label">Nom *</label>
                <input class="form-input" v-model="modal.form.nom" placeholder="Emplacement 1" />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">Capacité max <span style="color:var(--gray-400);font-size:.74rem;">(optionnel)</span></label>
              <input class="form-input" type="number" min="0" v-model.number="modal.form.capaciteMax" placeholder="Illimité" style="max-width:200px;" />
            </div>
          </template>
          <div class="form-error" v-if="modal.erreur">{{ modal.erreur }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modal.visible = false">Annuler</button>
          <button class="btn btn-primary" @click="sauvegarder" :disabled="modal.chargement">
            {{ modal.chargement ? 'Enregistrement...' : (modal.modeEdition ? 'Mettre à jour' : 'Créer') }}
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL CONFIRMATION DÉSACTIVATION -->
    <div class="modal-overlay" v-if="confirm.visible" @click.self="confirm.visible = false">
      <div class="modal" style="max-width:400px;">
        <div class="modal-header">
          <h3 class="modal-title">Désactiver {{ typeLabel(confirm.type) }}</h3>
          <button class="modal-close" @click="confirm.visible = false">✕</button>
        </div>
        <div class="modal-body">
          <p style="color:var(--gray-700);font-size:.9rem;line-height:1.6;">
            Confirmer la désactivation de <strong>{{ confirm.cible?.nom }}</strong> ?
          </p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="confirm.visible = false">Annuler</button>
          <button class="btn btn-danger" @click="confirmerDesactivation" :disabled="confirm.chargement">
            {{ confirm.chargement ? 'En cours...' : 'Désactiver' }}
          </button>
        </div>
      </div>
    </div>

    <!-- TOAST -->
    <transition name="toast-slide">
      <div v-if="toast.visible" :class="['toast-notif', toast.type === 'succes' ? 'toast-succes' : 'toast-erreur']">
        <div class="toast-icon">
          <svg v-if="toast.type==='succes'" width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
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
import { rayonApi, etagereApi, emplacementApi, entrepotApi, zoneApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'

const formVide = () => ({ nom: '', code: '', description: '', zoneId: null, rayonId: null, etagereId: null, niveaux: null, capaciteMax: null })

export default {
  name: 'EmplacementsPage',
  data() {
    return {
      rayons: [], etageres: [], emplacements: [],
      entrepots: [], zones: [],
      niveau: 'rayon',
      rayonSelectionne: null,
      etagereSelectionnee: null,
      filtreEntrepotId: null,
      filtreZoneId: null,
      recherche: '',
      chargement: false,
      modal: { visible: false, type: 'rayon', titre: '', modeEdition: false, editId: null, form: formVide(), erreur: '', chargement: false },
      confirm: { visible: false, type: '', cible: null, chargement: false },
      toast: { visible: false, type: 'succes', message: '', duree: 4000, _timer: null }
    }
  },
  computed: {
    peutEcrire() { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    zonesFiltrees() {
      if (!this.filtreEntrepotId) return this.zones
      return this.zones.filter(z => z.entrepotId === this.filtreEntrepotId)
    },
    listeFiltree() {
      let res = this.rayons
      if (this.filtreZoneId)      res = res.filter(r => r.zoneId === this.filtreZoneId)
      else if (this.filtreEntrepotId) res = res.filter(r => r.entrepotId === this.filtreEntrepotId)
      const q = this.recherche.toLowerCase().trim()
      if (!q) return res
      return res.filter(r =>
        r.nom?.toLowerCase().includes(q) ||
        r.code?.toLowerCase().includes(q) ||
        r.zoneNom?.toLowerCase().includes(q) ||
        r.entrepotNom?.toLowerCase().includes(q)
      )
    }
  },
  async mounted() {
    await Promise.all([this.chargerRayons(), this.chargerEntrepots(), this.chargerZones()])
  },
  beforeUnmount() { clearTimeout(this.toast._timer) },
  methods: {
    allerNiveau(n) { this.niveau = n; this.recherche = '' },
    async selectionnerRayon(rayon) {
      this.rayonSelectionne = rayon
      this.niveau = 'etagere'
      await this.chargerEtageres(rayon.id)
    },
    async selectionnerEtagere(etagere) {
      this.etagereSelectionnee = etagere
      this.niveau = 'emplacement'
      await this.chargerEmplacements(etagere.id)
    },
    async chargerRayons() {
      this.chargement = true
      try { this.rayons = (await rayonApi.findAll()).data }
      catch { this.afficherToast('Impossible de charger les rayons.', 'erreur') }
      finally { this.chargement = false }
    },
    async chargerEtageres(rayonId) {
      this.chargement = true
      try { this.etageres = (await etagereApi.findAll(rayonId)).data }
      finally { this.chargement = false }
    },
    async chargerEmplacements(etagereId) {
      this.chargement = true
      try { this.emplacements = (await emplacementApi.findByEtagere(etagereId)).data }
      finally { this.chargement = false }
    },
    async chargerEntrepots() { this.entrepots = (await entrepotApi.findAll()).data },
    async chargerZones()     { this.zones     = (await zoneApi.findAll()).data    },
    ouvrirModal(type, item = null) {
      this.modal.type       = type
      this.modal.modeEdition = !!item
      this.modal.editId     = item?.id || null
      this.modal.erreur     = ''
      this.modal.form       = formVide()
      if (item) {
        Object.assign(this.modal.form, {
          nom: item.nom, code: item.code, description: item.description || '',
          niveaux: item.niveaux, capaciteMax: item.capaciteMax,
          zoneId: item.zoneId, rayonId: item.rayonId, etagereId: item.etagereId
        })
      } else {
        if (type === 'etagere' && this.rayonSelectionne)     this.modal.form.rayonId   = this.rayonSelectionne.id
        if (type === 'emplacement' && this.etagereSelectionnee) this.modal.form.etagereId = this.etagereSelectionnee.id
      }
      const labels = { rayon: 'rayon', etagere: 'étagère', emplacement: 'emplacement' }
      this.modal.titre  = (item ? 'Modifier le ' : 'Nouveau ') + labels[type]
      this.modal.visible = true
    },
    async sauvegarder() {
      this.modal.erreur = ''
      const f = this.modal.form
      if (!f.nom?.trim()) { this.modal.erreur = 'Le nom est requis.'; return }
      if (this.modal.type === 'rayon'       && !f.zoneId)   { this.modal.erreur = 'La zone est requise.'; return }
      if (this.modal.type === 'etagere'     && !f.rayonId)  { this.modal.erreur = 'Le rayon est requis.'; return }
      if (this.modal.type === 'emplacement' && !f.code?.trim()) { this.modal.erreur = 'Le code est requis.'; return }
      this.modal.chargement = true
      try {
        const dto = { ...f }
        if (this.modal.type === 'rayon') {
          if (this.modal.modeEdition) await rayonApi.modifier(this.modal.editId, dto)
          else                        await rayonApi.creer(dto)
          await this.chargerRayons()
        } else if (this.modal.type === 'etagere') {
          if (this.modal.modeEdition) await etagereApi.modifier(this.modal.editId, dto)
          else                        await etagereApi.creer(dto)
          if (this.rayonSelectionne) await this.chargerEtageres(this.rayonSelectionne.id)
        } else {
          dto.etagereId = this.etagereSelectionnee?.id
          if (this.modal.modeEdition) await emplacementApi.modifier(this.modal.editId, dto)
          else                        await emplacementApi.creer(dto)
          if (this.etagereSelectionnee) await this.chargerEmplacements(this.etagereSelectionnee.id)
        }
        this.modal.visible = false
        this.afficherToast(this.modal.modeEdition ? 'Mis à jour.' : 'Créé avec succès.', 'succes')
      } catch (e) {
        this.modal.erreur = e.response?.data?.message || 'Une erreur est survenue.'
      } finally { this.modal.chargement = false }
    },
    demanderDesactivation(type, item) { this.confirm = { visible: true, type, cible: item, chargement: false } },
    async confirmerDesactivation() {
      this.confirm.chargement = true
      try {
        const { type, cible } = this.confirm
        if      (type === 'rayon')       await rayonApi.desactiver(cible.id)
        else if (type === 'etagere')     await etagereApi.desactiver(cible.id)
        else if (type === 'emplacement') await emplacementApi.desactiver(cible.id)
        this.confirm.visible = false
        this.afficherToast(this.typeLabel(type) + ' désactivé(e).', 'succes')
        if (type === 'rayon')            await this.chargerRayons()
        else if (type === 'etagere')     await this.chargerEtageres(this.rayonSelectionne.id)
        else                             await this.chargerEmplacements(this.etagereSelectionnee.id)
      } catch (e) {
        this.confirm.visible = false
        this.afficherToast(e.response?.data?.message || 'Impossible de désactiver.', 'erreur')
      } finally { this.confirm.chargement = false }
    },
    typeLabel(type) { return { rayon: 'Rayon', etagere: 'Étagère', emplacement: 'Emplacement' }[type] || type },
    afficherToast(message, type = 'succes', duree = 4000) {
      clearTimeout(this.toast._timer)
      this.toast = { visible: true, type, message, duree, _timer: null }
      this.toast._timer = setTimeout(() => { this.toast.visible = false }, duree)
    },
    fermerToast() { clearTimeout(this.toast._timer); this.toast.visible = false }
  }
}
</script>

<style scoped>
.breadcrumb-nav { display:flex;align-items:center;gap:6px;margin-bottom:16px;flex-wrap:wrap; }
.breadcrumb-item {
  display:flex;align-items:center;gap:5px;
  padding:7px 14px;border-radius:999px;
  border:1px solid var(--gray-200);background:var(--white);
  color:var(--gray-500);font-size:.82rem;font-weight:500;cursor:pointer;transition:all .15s;
}
.breadcrumb-item.active { background:var(--navy);color:#fff;border-color:var(--navy); }
.breadcrumb-item:hover:not(:disabled):not(.active) { background:var(--gray-50);color:var(--navy);border-color:var(--navy); }
.breadcrumb-item:disabled { opacity:.4;cursor:not-allowed; }
.breadcrumb-sep { color:var(--gray-300);font-size:1.1rem; }
.empty-state { text-align:center;padding:40px 20px;color:var(--gray-400);font-size:.9rem; }
.ref-code { font-size:.82rem;background:var(--navy-xlight);color:var(--navy);padding:2px 8px;border-radius:4px;font-family:monospace;font-weight:600; }
.ref-code-lg { font-size:.76rem;background:var(--gray-100);color:var(--gray-700);padding:3px 8px;border-radius:4px;font-family:monospace; }
.row-inactive { opacity:.5; }
.chemin { font-size:.82rem;color:var(--gray-500); }
/* Toast */
.toast-notif { position:fixed;top:24px;right:24px;z-index:9999;display:flex;align-items:flex-start;gap:12px;border-radius:var(--radius);box-shadow:var(--shadow-lg);padding:16px 14px 12px 16px;max-width:420px;min-width:280px; }
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
