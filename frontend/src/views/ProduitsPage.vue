<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Produits</h1>
      <p class="page-subtitle">Catalogue des produits — Module 4</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/>
            <path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input v-model="recherche" placeholder="Référence, nom, catégorie..." />
        </div>
        <button v-if="peutEcrire" class="btn btn-primary" @click="ouvrirModal()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          </svg>
          Nouveau produit
        </button>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Référence</th><th>Nom</th><th>Catégorie</th>
              <th>Prix achat</th><th>Prix vente</th><th>Marge</th>
              <th v-if="peutEcrire || peutSupprimer" style="text-align:right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="chargement">
              <td colspan="7" style="text-align:center;padding:32px;color:var(--gray-400);">Chargement...</td>
            </tr>
            <tr v-else-if="listeFiltree.length === 0">
              <td colspan="7" style="text-align:center;padding:32px;color:var(--gray-400);">Aucun produit trouvé.</td>
            </tr>
            <tr v-for="p in listeFiltree" :key="p.id">
              <td>
                <code style="background:var(--navy-xlight);color:var(--navy);padding:3px 8px;border-radius:4px;font-size:.78rem;font-weight:600;">
                  {{ p.reference }}
                </code>
              </td>
              <td>
                <span style="font-weight:600;color:var(--gray-900);">{{ p.nom }}</span>
                <p v-if="p.description" style="font-size:.75rem;color:var(--gray-400);margin-top:1px;">{{ p.description }}</p>
              </td>
              <td>
                <span class="badge badge-info" v-if="p.categorieNom">{{ p.categorieNom }}</span>
                <span v-else style="color:var(--gray-400);">—</span>
              </td>
              <td style="font-size:.875rem;">{{ p.prixAchat != null ? p.prixAchat.toFixed(2) + ' €' : '—' }}</td>
              <td style="font-size:.875rem;font-weight:600;">{{ p.prixVente != null ? p.prixVente.toFixed(2) + ' €' : '—' }}</td>
              <td>
                <span v-if="p.prixAchat && p.prixVente" class="badge"
                  :class="marge(p) >= 20 ? 'badge-success' : marge(p) >= 10 ? 'badge-warning' : 'badge-danger'">
                  {{ marge(p) }}%
                </span>
                <span v-else style="color:var(--gray-400);">—</span>
              </td>
              <td v-if="peutEcrire || peutSupprimer" style="text-align:right;">
                <div style="display:flex;gap:6px;justify-content:flex-end;">
                  <button v-if="peutEcrire" class="btn btn-outline btn-sm" @click="ouvrirModal(p)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="1.8"/>
                    </svg>
                    Éditer
                  </button>
                  <!-- Bouton QR Code -->
                  <button class="btn btn-outline btn-sm" @click="ouvrirQr(p)" title="Générer QR Code">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <rect x="3" y="3" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.8"/>
                      <rect x="14" y="3" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.8"/>
                      <rect x="3" y="14" width="7" height="7" rx="1" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M14 14h2M14 18h2M18 14h2M18 18h2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                    </svg>
                    QR
                  </button>
                  <button v-if="peutSupprimer" class="btn btn-danger btn-sm" @click="demanderSuppression(p)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                      <polyline points="3 6 5 6 21 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                      <path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" stroke="currentColor" stroke-width="1.8"/>
                      <path d="M10 11v6M14 11v6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                    </svg>
                    Supprimer
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
          <h3 class="modal-title">{{ form.id ? 'Modifier le produit' : 'Nouveau produit' }}</h3>
          <button class="modal-close" @click="modal = false">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Référence *</label>
              <input class="form-input" v-model="form.reference" placeholder="PRD-001" />
            </div>
            <div class="form-group">
              <label class="form-label">Code-barres</label>
              <input class="form-input" v-model="form.codeBarre" placeholder="1234567890123" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">Nom *</label>
            <input class="form-input" v-model="form.nom" placeholder="Laptop Dell XPS" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Catégorie</label>
              <select class="form-select" v-model.number="form.categorieId">
                <option :value="null">— Sans catégorie —</option>
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.nom }}</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Description</label>
              <input class="form-input" v-model="form.description" placeholder="Description courte" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Prix d'achat (€)</label>
              <input class="form-input" type="number" step="0.01" min="0" v-model.number="form.prixAchat" />
            </div>
            <div class="form-group">
              <label class="form-label">Prix de vente (€)</label>
              <input class="form-input" type="number" step="0.01" min="0" v-model.number="form.prixVente" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Poids (kg)</label>
              <input class="form-input" type="number" step="0.01" min="0" v-model.number="form.poids" />
            </div>
            <div class="form-group">
              <label class="form-label">Volume (m³)</label>
              <input class="form-input" type="number" step="0.0001" min="0" v-model.number="form.volume" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">
              Stock minimum
              <span style="font-weight:400;color:var(--gray-400);font-size:.78rem;margin-left:4px;">
                — une alerte se déclenche quand le stock passe en dessous
              </span>
            </label>
            <input class="form-input" type="number" min="0" v-model.number="form.stockMinDefaut"
              placeholder="0 = pas d'alerte" style="max-width:180px;" />
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
      titre="Supprimer le produit"
      :message="`Voulez-vous supprimer « ${confirm.cible?.nom} » (réf. ${confirm.cible?.reference}) ? Cette action est irréversible.`"
      type="danger"
      label-confirmer="Supprimer"
      @confirmer="confirmerSuppression"
      @annuler="confirm.visible = false"
    />

    <!-- Modal QR Code -->
    <div class="modal-overlay" v-if="modalQr" @click.self="modalQr = false">
      <div class="modal" style="max-width:380px;">
        <div class="modal-header">
          <h3 class="modal-title">QR Code — {{ produitQr?.nom }}</h3>
          <button class="modal-close" @click="modalQr = false">✕</button>
        </div>
        <div class="modal-body" style="text-align:center;">
          <img v-if="produitQr" :src="qrUrl(produitQr.id)" :alt="produitQr.nom"
            style="width:240px;height:240px;border:1px solid var(--gray-200);border-radius:8px;" />
          <p style="margin-top:10px;font-size:.84rem;color:var(--gray-600);">
            <code style="background:var(--gray-100);padding:2px 6px;border-radius:4px;">{{ produitQr?.reference }}</code>
          </p>
          <p style="font-size:.78rem;color:var(--gray-400);margin-top:6px;">
            Scannez avec l'appareil photo ou l'onglet Scanner
          </p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="modalQr = false">Fermer</button>
          <button class="btn btn-primary" @click="imprimerQrProduit">Imprimer</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { produitApi, categoryApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'
import ConfirmModal from '../components/ConfirmModal.vue'

export default {
  name: 'ProduitsPage',
  components: { ConfirmModal },
  data() {
    return {
      liste: [], categories: [], chargement: true, modal: false, erreur: '', recherche: '',
      confirm: { visible: false, cible: null },
      form: { id: null, reference: '', codeBarre: '', nom: '', categorieId: null, description: '', prixAchat: null, prixVente: null, poids: null, volume: null, stockMinDefaut: 0 },
      // QR Code
      modalQr: false,
      produitQr: null
    }
  },
  computed: {
    peutEcrire()    { return authStore.aUnRole('ADMIN', 'GESTIONNAIRE') },
    peutSupprimer() { return authStore.aRole('ADMIN') },
    listeFiltree() {
      const q = this.recherche.toLowerCase()
      if (!q) return this.liste
      return this.liste.filter(p =>
        p.nom?.toLowerCase().includes(q) ||
        p.reference?.toLowerCase().includes(q) ||
        p.categorieNom?.toLowerCase().includes(q)
      )
    }
  },
  async mounted() {
    await Promise.all([this.charger(), this.chargerCategories()])
  },
  methods: {
    marge(p) { return Math.round(((p.prixVente - p.prixAchat) / p.prixVente) * 100) },
    async charger() {
      this.chargement = true
      try { const r = await produitApi.findAll(); this.liste = r.data }
      finally { this.chargement = false }
    },
    async chargerCategories() {
      const r = await categoryApi.findAll()
      this.categories = r.data.filter(c => c.actif)
    },
    ouvrirModal(p = null) {
      this.erreur = ''
      this.form = p
        ? { id: p.id, reference: p.reference, codeBarre: p.codeBarre, nom: p.nom, categorieId: p.categorieId || null, description: p.description, prixAchat: p.prixAchat, prixVente: p.prixVente, poids: p.poids, volume: p.volume, stockMinDefaut: p.stockMinDefaut || 0 }
        : { id: null, reference: '', codeBarre: '', nom: '', categorieId: null, description: '', prixAchat: null, prixVente: null, poids: null, volume: null, stockMinDefaut: 0 }
      this.modal = true
    },
    async sauvegarder() {
      this.erreur = ''
      try {
        if (this.form.id) await produitApi.modifier(this.form.id, this.form)
        else await produitApi.creer(this.form)
        this.modal = false
        this.charger()
      } catch (e) {
        this.erreur = e.response?.data?.message || 'Erreur lors de la sauvegarde.'
      }
    },
    demanderSuppression(p) { this.confirm = { visible: true, cible: p } },
    async confirmerSuppression() {
      await produitApi.supprimer(this.confirm.cible.id)
      this.confirm = { visible: false, cible: null }
      this.charger()
    },
    // ── QR Code ──────────────────────────────────────────────────────
    ouvrirQr(p) {
      this.produitQr = p
      this.modalQr   = true
    },
    qrUrl(id) {
      // /api/qr/produit/{id} — image PNG servie par le backend
      return `/api/qr/produit/${id}?t=${Date.now()}`
    },
    imprimerQrProduit() {
      if (!this.produitQr) return
      const win = window.open('', '_blank')
      win.document.write(`
        <html><head><title>QR — ${this.produitQr.nom}</title>
        <style>
          body { font-family: sans-serif; text-align: center; padding: 40px; }
          img  { width: 280px; height: 280px; border: 1px solid #e2e8f0; border-radius: 8px; }
          h2   { font-size: 16px; margin-top: 14px; color: #1e3a5f; }
          code { font-size: 13px; background: #f1f5f9; padding: 3px 8px; border-radius: 4px; }
        </style></head>
        <body>
          <img src="${this.qrUrl(this.produitQr.id)}" />
          <h2>${this.produitQr.nom}</h2>
          <code>${this.produitQr.reference}</code>
          <script>window.onload = () => { window.print(); window.close(); }<\/script>
        </body></html>
      `)
      win.document.close()
    }
  }
}
</script>
