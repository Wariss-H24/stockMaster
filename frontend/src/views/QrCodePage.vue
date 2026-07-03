<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">QR Code & Scanner</h1>
      <p class="page-subtitle">Module 18 — Générer, imprimer et scanner les codes QR</p>
    </div>

    <!-- Onglets -->
    <div class="tabs">
      <button class="tab" :class="{ active: onglet === 'generer' }" @click="onglet = 'generer'">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><rect x="3" y="3" width="7" height="7" stroke="currentColor" stroke-width="1.8"/><rect x="14" y="3" width="7" height="7" stroke="currentColor" stroke-width="1.8"/><rect x="3" y="14" width="7" height="7" stroke="currentColor" stroke-width="1.8"/><path d="M14 14h2M14 18h2M18 14h2M18 18h2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
        Générer un QR
      </button>
      <button class="tab" :class="{ active: onglet === 'scanner' }" @click="onglet = 'scanner'; demarrerScan()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M4 8V6a2 2 0 012-2h2M8 20H6a2 2 0 01-2-2v-2M16 4h2a2 2 0 012 2v2M20 16v2a2 2 0 01-2 2h-2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/><line x1="4" y1="12" x2="20" y2="12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
        Scanner
      </button>
    </div>

    <!-- ═══════════════════════ ONGLET GÉNÉRER ═══════════════════════ -->
    <div v-if="onglet === 'generer'">
      <div class="gen-layout">
        <!-- Panneau gauche : sélection -->
        <div class="card gen-panel">
          <h3 class="panel-title">Sélectionner un élément</h3>

          <div class="form-group">
            <label class="form-label">Type</label>
            <div class="type-selector">
              <button class="type-btn" :class="{ active: typeQr === 'produit' }" @click="typeQr = 'produit'; qrGenere = null">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M20 7H4a1 1 0 00-1 1v10a1 1 0 001 1h16a1 1 0 001-1V8a1 1 0 00-1-1z" stroke="currentColor" stroke-width="1.8"/></svg>
                Produit
              </button>
              <button class="type-btn" :class="{ active: typeQr === 'emplacement' }" @click="typeQr = 'emplacement'; qrGenere = null">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" stroke="currentColor" stroke-width="1.8"/></svg>
                Emplacement
              </button>
            </div>
          </div>

          <!-- Produit -->
          <div v-if="typeQr === 'produit'" class="form-group">
            <label class="form-label">Produit *</label>
            <div class="search-box" style="margin-bottom:8px;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/><path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
              <input v-model="rechercheProduit" placeholder="Nom ou référence..." />
            </div>
            <div class="liste-select">
              <div v-for="p in produitsFiltres" :key="p.id"
                class="liste-item" :class="{ selected: selectionId === p.id }"
                @click="selectionner(p)">
                <div>
                  <span class="item-nom">{{ p.nom }}</span>
                  <code class="item-ref">{{ p.reference }}</code>
                </div>
                <span v-if="selectionId === p.id" class="checkmark">✓</span>
              </div>
              <div v-if="produitsFiltres.length === 0" class="liste-vide">Aucun produit trouvé</div>
            </div>
          </div>

          <!-- Emplacement -->
          <div v-if="typeQr === 'emplacement'" class="form-group">
            <label class="form-label">Emplacement *</label>
            <div class="search-box" style="margin-bottom:8px;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.8"/><path d="M20 20l-3-3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
              <input v-model="rechercheEmplacement" placeholder="Code ou emplacement..." />
            </div>
            <div class="liste-select">
              <div v-for="e in emplacementsFiltres" :key="e.id"
                class="liste-item" :class="{ selected: selectionId === e.id }"
                @click="selectionner(e)">
                <div>
                  <code class="item-ref">{{ e.code }}</code>
                  <span class="item-chemin">{{ e.codeComplet }}</span>
                </div>
                <span v-if="selectionId === e.id" class="checkmark">✓</span>
              </div>
              <div v-if="emplacementsFiltres.length === 0" class="liste-vide">Aucun emplacement trouvé</div>
            </div>
          </div>

          <button class="btn btn-primary" style="width:100%;" @click="genererQr"
            :disabled="!selectionId || chargementQr">
            {{ chargementQr ? 'Génération...' : 'Générer le QR Code' }}
          </button>
        </div>

        <!-- Panneau droit : QR affiché -->
        <div class="card gen-panel" v-if="qrGenere">
          <h3 class="panel-title">QR Code généré</h3>

          <div class="qr-display">
            <img :src="qrGenere.url" :alt="qrGenere.label" class="qr-img" />
            <p class="qr-label">{{ qrGenere.label }}</p>
            <p class="qr-type-badge" :class="typeQr === 'produit' ? 'badge-blue' : 'badge-green'">
              {{ typeQr === 'produit' ? 'PRODUCT' : 'LOCATION' }}
            </p>
          </div>

          <div class="qr-actions">
            <button class="btn btn-outline" @click="imprimerQr">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="M6 9V2h12v7M6 18H4a2 2 0 01-2-2v-5a2 2 0 012-2h16a2 2 0 012 2v5a2 2 0 01-2 2h-2" stroke="currentColor" stroke-width="1.8"/><path d="M6 14h12v8H6z" stroke="currentColor" stroke-width="1.8"/></svg>
              Imprimer
            </button>
            <button class="btn btn-primary" @click="telechargerQr">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
              Télécharger PNG
            </button>
          </div>

          <!-- Instructions scan -->
          <div class="info-scan">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
            <span>Scannez avec l'appareil photo de votre téléphone ou utilisez l'onglet "Scanner"</span>
          </div>
        </div>

        <!-- Placeholder quand rien n'est généré -->
        <div class="card gen-panel placeholder-panel" v-else>
          <div class="placeholder-content">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" style="opacity:.15;margin-bottom:16px;"><rect x="3" y="3" width="7" height="7" stroke="currentColor" stroke-width="1.5"/><rect x="14" y="3" width="7" height="7" stroke="currentColor" stroke-width="1.5"/><rect x="3" y="14" width="7" height="7" stroke="currentColor" stroke-width="1.5"/><path d="M14 14h2M14 18h2M18 14h2M18 18h2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
            <p style="color:var(--gray-400);font-size:.9rem;">Sélectionnez un produit ou un emplacement<br>puis cliquez sur "Générer le QR Code"</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══════════════════════ ONGLET SCANNER ═══════════════════════ -->
    <div v-if="onglet === 'scanner'">
      <div class="scan-layout">
        <!-- Caméra -->
        <div class="card scan-panel">
          <h3 class="panel-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"><path d="M4 8V6a2 2 0 012-2h2M8 20H6a2 2 0 01-2-2v-2M16 4h2a2 2 0 012 2v2M20 16v2a2 2 0 01-2 2h-2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
            Scan en direct
          </h3>

          <div class="video-wrapper">
            <video ref="videoEl" class="video-feed" autoplay muted playsinline></video>
            <canvas ref="canvasEl" style="display:none;"></canvas>
            <div class="scan-overlay">
              <div class="scan-frame"></div>
              <p class="scan-hint">Pointez le QR Code dans le cadre</p>
            </div>
            <div v-if="scanEnCours" class="scan-indicator">
              <div class="scan-pulse"></div>
              Scan en cours...
            </div>
          </div>

          <div class="scan-controls">
            <button v-if="!scanActif" class="btn btn-primary" @click="demarrerScan">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none"><polygon points="5 3 19 12 5 21 5 3" fill="currentColor"/></svg>
              Démarrer la caméra
            </button>
            <button v-else class="btn btn-danger" @click="arreterScan">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none"><rect x="4" y="4" width="16" height="16" fill="currentColor"/></svg>
              Arrêter
            </button>
            <p v-if="erreurCamera" class="err-camera">{{ erreurCamera }}</p>
          </div>
        </div>

        <!-- Résultat du scan -->
        <div class="card scan-panel" v-if="resultatScan">

          <!-- RÉSULTAT PRODUIT -->
          <div v-if="resultatScan.type === 'PRODUCT'">
            <div class="result-header">
              <div class="result-icon product-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M20 7H4a1 1 0 00-1 1v10a1 1 0 001 1h16a1 1 0 001-1V8a1 1 0 00-1-1z" stroke="currentColor" stroke-width="2"/></svg>
              </div>
              <div>
                <p class="result-type">Produit scanné</p>
                <h2 class="result-nom">{{ resultatScan.produitNom }}</h2>
              </div>
            </div>

            <div class="result-grid">
              <div class="result-field">
                <span class="field-label">Référence</span>
                <code class="field-value">{{ resultatScan.produitReference }}</code>
              </div>
              <div class="result-field" v-if="resultatScan.produitCategorie">
                <span class="field-label">Catégorie</span>
                <span class="field-value badge badge-info">{{ resultatScan.produitCategorie }}</span>
              </div>
              <div class="result-field" v-if="resultatScan.produitDescription">
                <span class="field-label">Description</span>
                <span class="field-value">{{ resultatScan.produitDescription }}</span>
              </div>
              <div class="result-field" v-if="resultatScan.produitPrixAchat">
                <span class="field-label">Prix achat</span>
                <span class="field-value">{{ formaterMontant(resultatScan.produitPrixAchat) }}</span>
              </div>
              <div class="result-field" v-if="resultatScan.produitPrixVente">
                <span class="field-label">Prix vente</span>
                <span class="field-value">{{ formaterMontant(resultatScan.produitPrixVente) }}</span>
              </div>
            </div>

            <!-- Stock total -->
            <div class="stock-total" :class="resultatScan.stockTotal > 0 ? 'stock-ok' : 'stock-vide'">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none"><path d="M4 7h16M4 12h16M4 17h16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
              <span>Stock total : <strong>{{ resultatScan.stockTotal }}</strong> unité(s)</span>
            </div>

            <!-- Détail par emplacement -->
            <h4 class="section-subtitle" v-if="resultatScan.stocks?.length">Localisation par entrepôt</h4>
            <div class="stock-detail-list">
              <div v-for="s in resultatScan.stocks" :key="s.stockId" class="stock-detail-item"
                :class="{ 'stock-critique': s.quantiteDisponible <= (s.stockMin || 0) && s.stockMin > 0 }">
                <div class="detail-left">
                  <p class="detail-entrepot">{{ s.entrepotNom }}</p>
                  <p class="detail-zone" v-if="s.zoneNom">{{ s.zoneNom }}</p>
                  <code v-if="s.emplacementCodeComplet" class="detail-emp">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" stroke="currentColor" stroke-width="2.5"/></svg>
                    {{ s.emplacementCodeComplet.split('/').pop() }}
                  </code>
                </div>
                <div class="detail-right">
                  <div class="qty-block">
                    <span class="qty-label">Disponible</span>
                    <span class="qty-val" :class="s.quantiteDisponible > 0 ? 'qty-ok' : 'qty-zero'">{{ s.quantiteDisponible }}</span>
                  </div>
                  <div class="qty-block" v-if="s.quantiteReservee > 0">
                    <span class="qty-label">Réservé</span>
                    <span class="qty-val qty-reserved">{{ s.quantiteReservee }}</span>
                  </div>
                  <div class="qty-block" v-if="s.quantiteTransit > 0">
                    <span class="qty-label">Transit</span>
                    <span class="qty-val qty-transit">{{ s.quantiteTransit }}</span>
                  </div>
                  <div v-if="s.stockMin > 0" class="qty-block">
                    <span class="qty-label">Min</span>
                    <span class="qty-val qty-min">{{ s.stockMin }}</span>
                  </div>
                </div>
              </div>
              <div v-if="!resultatScan.stocks?.length" class="liste-vide">Aucun stock enregistré</div>
            </div>
          </div>

          <!-- RÉSULTAT EMPLACEMENT -->
          <div v-else-if="resultatScan.type === 'LOCATION'">
            <div class="result-header">
              <div class="result-icon location-icon">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" stroke="currentColor" stroke-width="2"/><circle cx="12" cy="9" r="2.5" stroke="currentColor" stroke-width="2"/></svg>
              </div>
              <div>
                <p class="result-type">Emplacement scanné</p>
                <h2 class="result-nom">{{ resultatScan.emplacementCode }}</h2>
              </div>
            </div>

            <!-- Chemin hiérarchique -->
            <div class="hierarchy-path">
              <span class="hier-item">{{ resultatScan.entrepotNom }}</span>
              <span class="hier-sep">›</span>
              <span class="hier-item">{{ resultatScan.zoneNom }}</span>
              <span class="hier-sep">›</span>
              <span class="hier-item">{{ resultatScan.rayonNom }}</span>
              <span class="hier-sep">›</span>
              <span class="hier-item">{{ resultatScan.etagereNom }}</span>
              <span class="hier-sep">›</span>
              <strong class="hier-item hier-current">{{ resultatScan.emplacementCode }}</strong>
            </div>

            <!-- Capacité -->
            <div v-if="resultatScan.capaciteMax" class="capacite-block">
              <div style="display:flex;justify-content:space-between;margin-bottom:6px;">
                <span style="font-size:.84rem;color:var(--gray-600);">Occupation</span>
                <span style="font-size:.84rem;font-weight:700;">
                  {{ resultatScan.capaciteUtilisee }} / {{ resultatScan.capaciteMax }}
                </span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill"
                  :class="tauxOccupation <= 60 ? 'progress-low' : tauxOccupation <= 85 ? 'progress-mid' : 'progress-high'"
                  :style="{ width: tauxOccupation + '%' }">
                </div>
              </div>
            </div>

            <!-- Produits présents -->
            <h4 class="section-subtitle">
              Produits présents
              <span class="badge badge-navy" style="margin-left:6px;">{{ (resultatScan.produitsPresents || []).length }}</span>
            </h4>
            <div class="produit-list">
              <div v-for="p in resultatScan.produitsPresents" :key="p.produitId" class="produit-item">
                <div>
                  <p class="produit-nom">{{ p.produitNom }}</p>
                  <code class="produit-ref">{{ p.produitReference }}</code>
                  <span v-if="p.categorieNom" class="badge badge-info" style="font-size:.7rem;margin-left:4px;">{{ p.categorieNom }}</span>
                </div>
                <div class="produit-qty">
                  <span class="qty-big" :class="p.statut === 'CRITIQUE' ? 'qty-crit' : p.statut === 'FAIBLE' ? 'qty-faible' : 'qty-ok'">
                    {{ p.quantiteDisponible }}
                  </span>
                  <span style="font-size:.7rem;color:var(--gray-400);">unité(s)</span>
                  <span class="badge" style="margin-top:4px;font-size:.65rem;"
                    :class="p.statut === 'CRITIQUE' ? 'badge-danger' : p.statut === 'FAIBLE' ? 'badge-warning' : 'badge-success'">
                    {{ p.statut }}
                  </span>
                </div>
              </div>
              <div v-if="!resultatScan.produitsPresents?.length" class="liste-vide">
                Aucun produit stocké à cet emplacement
              </div>
            </div>
          </div>

          <button class="btn btn-outline" style="margin-top:16px;width:100%;" @click="resultatScan = null; demarrerScan()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M4 4v5h.58M20 20v-5h-.58M4.58 9A8 8 0 0120 13.42M19.42 15A8 8 0 014 10.58" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
            Scanner un autre QR
          </button>
        </div>

        <!-- Placeholder scan -->
        <div class="card scan-panel placeholder-panel" v-else>
          <div class="placeholder-content">
            <svg width="70" height="70" viewBox="0 0 24 24" fill="none" style="opacity:.15;margin-bottom:16px;"><path d="M4 8V6a2 2 0 012-2h2M8 20H6a2 2 0 01-2-2v-2M16 4h2a2 2 0 012 2v2M20 16v2a2 2 0 01-2 2h-2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><line x1="4" y1="12" x2="20" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
            <p style="color:var(--gray-400);font-size:.9rem;">
              Démarrez la caméra et scannez un QR Code.<br>
              Les informations s'afficheront ici.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { BrowserMultiFormatReader } from '@zxing/browser'
import { NotFoundException } from '@zxing/library'
import { produitApi, emplacementApi, qrApi } from '../services/api.js'
import { authStore } from '../services/authStore.js'

export default {
  name: 'QrCodePage',

  data() {
    return {
      onglet: 'generer',
      // Génération
      typeQr: 'produit',
      produits: [],
      emplacements: [],
      rechercheProduit: '',
      rechercheEmplacement: '',
      selectionId: null,
      selectionItem: null,
      chargementQr: false,
      qrGenere: null,
      // Scanner
      scanActif: false,
      scanEnCours: false,
      erreurCamera: '',
      resultatScan: null,
      reader: null,
    }
  },

  computed: {
    produitsFiltres() {
      const q = this.rechercheProduit.toLowerCase()
      if (!q) return this.produits.slice(0, 30)
      return this.produits.filter(p =>
        p.nom?.toLowerCase().includes(q) || p.reference?.toLowerCase().includes(q)
      ).slice(0, 20)
    },
    emplacementsFiltres() {
      const q = this.rechercheEmplacement.toLowerCase()
      if (!q) return this.emplacements.slice(0, 30)
      return this.emplacements.filter(e =>
        e.code?.toLowerCase().includes(q) || e.codeComplet?.toLowerCase().includes(q)
      ).slice(0, 20)
    },
    tauxOccupation() {
      if (!this.resultatScan?.capaciteMax) return 0
      return Math.round((this.resultatScan.capaciteUtilisee / this.resultatScan.capaciteMax) * 100)
    }
  },

  async mounted() {
    await this.chargerDonnees()
    this.traiterParamsUrl()
  },

  beforeUnmount() {
    this.arreterScan()
  },

  methods: {
    // ── Chargement ─────────────────────────────────────────────────────
    async chargerDonnees() {
      try {
        const [p, e] = await Promise.all([produitApi.findAll(), emplacementApi.findAll()])
        this.produits     = p.data
        this.emplacements = e.data
      } catch { /* silencieux */ }
    },

    selectionner(item) {
      this.selectionId   = item.id
      this.selectionItem = item
      this.qrGenere      = null
    },

    // ── Génération QR ──────────────────────────────────────────────────
    async genererQr() {
      if (!this.selectionId) return
      this.chargementQr = true
      try {
        // Télécharger l'image via Axios (avec JWT) et créer un blob URL local
        const res = this.typeQr === 'produit'
          ? await qrApi.getProduitPng(this.selectionId)
          : await qrApi.getEmplacementPng(this.selectionId)

        // Révoquer l'ancienne blob URL si elle existe
        if (this.qrGenere?.blobUrl) URL.revokeObjectURL(this.qrGenere.blobUrl)

        const blobUrl = URL.createObjectURL(res.data)

        this.qrGenere = {
          url:     blobUrl,   // blob URL locale — pas de problème d'auth
          blobUrl: blobUrl,   // garde une ref pour révoquer plus tard
          label:   this.typeQr === 'produit'
            ? `${this.selectionItem.nom} — ${this.selectionItem.reference}`
            : `${this.selectionItem.code} — ${this.selectionItem.codeComplet}`,
          id: this.selectionId
        }
      } catch (e) {
        console.error('Erreur génération QR:', e)
      } finally {
        this.chargementQr = false
      }
    },

    imprimerQr() {
      const win = window.open('', '_blank')
      win.document.write(`
        <html><head><title>QR — ${this.qrGenere.label}</title>
        <style>
          body { font-family: sans-serif; text-align: center; padding: 40px; }
          img  { width: 300px; height: 300px; border: 1px solid #e2e8f0; border-radius: 8px; }
          h2   { font-size: 16px; margin-top: 16px; }
          p    { font-size: 12px; color: #666; }
        </style></head>
        <body>
          <img src="${this.qrGenere.url}" />
          <h2>${this.qrGenere.label}</h2>
          <p>${this.typeQr === 'produit' ? 'PRODUCT' : 'LOCATION'} — ID ${this.qrGenere.id}</p>
          <script>window.onload = () => { window.print(); window.close(); }<\/script>
        </body></html>
      `)
      win.document.close()
    },

    async telechargerQr() {
      const res = this.typeQr === 'produit'
        ? await qrApi.getProduitPng(this.selectionId)
        : await qrApi.getEmplacementPng(this.selectionId)
      const url = URL.createObjectURL(res.data)
      const a   = document.createElement('a')
      a.href     = url
      a.download = `qr-${this.typeQr}-${this.selectionId}.png`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    },

    // ── Scanner caméra ─────────────────────────────────────────────────
    async demarrerScan() {
      if (this.onglet !== 'scanner') return
      this.erreurCamera = ''
      this.resultatScan = null
      this.scanActif    = true
      this.scanEnCours  = true

      try {
        // Créer un nouveau reader à chaque démarrage
        this.reader = new BrowserMultiFormatReader()

        const devices = await BrowserMultiFormatReader.listVideoInputDevices()
        if (!devices || devices.length === 0) {
          throw new Error('Aucune caméra détectée sur cet appareil.')
        }

        // Préférer la caméra arrière (mobile)
        const device = devices.find(d => /back|rear|environment/i.test(d.label))
          || devices[devices.length - 1]

        await this.reader.decodeFromVideoDevice(
          device.deviceId,
          this.$refs.videoEl,
          async (result, err) => {
            if (result) {
              this.scanEnCours = false
              await this.traiterResultatScan(result.getText())
            }
            // Ignorer les erreurs NotFoundException (pas de QR dans le champ)
            if (err && !(err instanceof NotFoundException)) {
              console.warn('Scan error:', err)
            }
          }
        )
      } catch (e) {
        this.scanActif   = false
        this.scanEnCours = false
        if (e.name === 'NotAllowedError') {
          this.erreurCamera = 'Accès caméra refusé. Autorisez l\'accès dans les paramètres du navigateur.'
        } else if (e.name === 'NotFoundError') {
          this.erreurCamera = 'Aucune caméra trouvée sur cet appareil.'
        } else {
          this.erreurCamera = e.message || 'Impossible d\'accéder à la caméra.'
        }
      }
    },

    arreterScan() {
      if (this.reader) {
        try {
          BrowserMultiFormatReader.releaseAllStreams()
        } catch { /* */ }
        this.reader = null
      }
      this.scanActif   = false
      this.scanEnCours = false
    },

    async traiterResultatScan(texteQr) {
      this.arreterScan()
      try {
        const url  = new URL(texteQr)
        const type = url.searchParams.get('type')
        const id   = url.searchParams.get('id')

        if (!type || !id) {
          this.erreurCamera = 'QR Code non reconnu par StockMaster.'
          setTimeout(() => this.demarrerScan(), 2500)
          return
        }

        const res = await qrApi.resoudre(type, Number(id))
        this.resultatScan = res.data
      } catch (e) {
        this.erreurCamera = 'QR Code non reconnu ou données introuvables.'
        setTimeout(() => { this.erreurCamera = ''; this.demarrerScan() }, 2500)
      }
    },

    /** Traiter les paramètres URL quand l'app est ouverte depuis un téléphone */
    async traiterParamsUrl() {
      const params = new URLSearchParams(window.location.search)
      const type   = params.get('type')
      const id     = params.get('id')
      if (type && id && authStore.estConnecte) {
        this.onglet = 'scanner'
        try {
          const res = await qrApi.resoudre(type, Number(id))
          this.resultatScan = res.data
          window.history.replaceState({}, document.title, '/qrcode')
        } catch { /* silencieux */ }
      }
    },

    formaterMontant(v) {
      return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(v || 0)
    }
  }
}
</script>

<style scoped>
/* ── Tabs ── */
.tabs { display:flex; gap:8px; margin-bottom:20px; }
.tab {
  display: flex; align-items: center; gap: 7px;
  padding: 9px 18px; border-radius: 999px;
  border: 1px solid var(--gray-200); background: var(--white);
  color: var(--gray-600); font-size: .85rem; font-weight: 600;
  cursor: pointer; transition: all .15s;
}
.tab.active { background: var(--navy); color: #fff; border-color: var(--navy); }
.tab:hover:not(.active) { background: var(--gray-50); border-color: var(--gray-400); }

/* ── Layout ── */
.gen-layout, .scan-layout {
  display: grid; grid-template-columns: 1fr 1fr; gap: 20px;
}
.gen-panel, .scan-panel { padding: 20px 24px; }
.panel-title {
  font-size: .95rem; font-weight: 700; color: var(--gray-900);
  margin-bottom: 18px; display: flex; align-items: center; gap: 8px;
}

/* ── Type selector ── */
.type-selector { display: flex; gap: 8px; margin-bottom: 16px; }
.type-btn {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px; border: 2px solid var(--gray-200); border-radius: var(--radius);
  background: var(--white); color: var(--gray-600); font-weight: 600;
  font-size: .84rem; cursor: pointer; transition: all .15s;
}
.type-btn.active { border-color: var(--navy); color: var(--navy); background: var(--navy-xlight); }

/* ── Liste sélection ── */
.liste-select {
  border: 1px solid var(--gray-200); border-radius: var(--radius-sm);
  max-height: 260px; overflow-y: auto;
}
.liste-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 12px; cursor: pointer; transition: background .1s;
  border-bottom: 1px solid var(--gray-100);
}
.liste-item:last-child { border-bottom: none; }
.liste-item:hover { background: var(--gray-50); }
.liste-item.selected { background: var(--navy-xlight); }
.item-nom { font-weight: 600; font-size: .85rem; color: var(--gray-900); display: block; }
.item-ref { font-size: .75rem; font-family: monospace; color: var(--gray-500); }
.item-chemin { font-size: .72rem; color: var(--gray-400); display: block; margin-top: 1px; }
.checkmark { color: var(--navy); font-weight: 700; font-size: 1.1rem; }
.liste-vide { padding: 20px; text-align: center; color: var(--gray-400); font-size: .84rem; }

/* ── QR affiché ── */
.qr-display { text-align: center; padding: 20px 0; }
.qr-img { width: 240px; height: 240px; border: 2px solid var(--gray-200); border-radius: var(--radius); }
.qr-label { font-weight: 600; font-size: .9rem; margin-top: 10px; color: var(--gray-800); }
.qr-type-badge { display: inline-block; margin-top: 6px; font-size: .72rem; font-weight: 700; padding: 2px 10px; border-radius: 999px; }
.badge-blue  { background: var(--blue-light); color: var(--blue-dark); }
.badge-green { background: var(--success-bg); color: var(--success); }
.qr-actions { display: flex; gap: 10px; margin-top: 16px; }
.qr-actions .btn { flex: 1; justify-content: center; }
.info-scan {
  display: flex; align-items: flex-start; gap: 8px; margin-top: 16px;
  background: #eff6ff; border: 1px solid #bfdbfe; border-radius: var(--radius-sm);
  padding: 10px 12px; font-size: .8rem; color: #1e40af;
}

/* ── Placeholder ── */
.placeholder-panel { display: flex; align-items: center; justify-content: center; min-height: 350px; }
.placeholder-content { text-align: center; }

/* ── Caméra ── */
.video-wrapper {
  position: relative; background: #000; border-radius: var(--radius);
  overflow: hidden; aspect-ratio: 4/3; max-height: 360px;
}
.video-feed { width: 100%; height: 100%; object-fit: cover; }
.scan-overlay {
  position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
}
.scan-frame {
  width: 200px; height: 200px;
  border: 3px solid rgba(255,255,255,.8);
  border-radius: 12px;
  box-shadow: 0 0 0 9999px rgba(0,0,0,.35);
  animation: frame-pulse 2s ease-in-out infinite;
}
@keyframes frame-pulse {
  0%, 100% { border-color: rgba(255,255,255,.8); }
  50%       { border-color: #2563eb; box-shadow: 0 0 0 9999px rgba(0,0,0,.4), 0 0 20px rgba(37,99,235,.6); }
}
.scan-hint { color: rgba(255,255,255,.85); font-size: .8rem; margin-top: 12px; text-shadow: 0 1px 2px rgba(0,0,0,.5); }
.scan-indicator {
  position: absolute; bottom: 12px; left: 50%; transform: translateX(-50%);
  display: flex; align-items: center; gap: 8px;
  background: rgba(37,99,235,.9); color: #fff; border-radius: 999px;
  padding: 6px 14px; font-size: .78rem; font-weight: 600;
}
.scan-pulse { width: 10px; height: 10px; border-radius: 50%; background: #fff; animation: blink 1s infinite; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: .3; } }
.scan-controls { margin-top: 14px; text-align: center; }
.err-camera { color: var(--danger); font-size: .84rem; margin-top: 8px; }

/* ── Résultat PRODUCT ── */
.result-header { display: flex; align-items: flex-start; gap: 14px; margin-bottom: 18px; }
.result-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.product-icon  { background: var(--blue-light);  color: var(--blue-dark); }
.location-icon { background: var(--success-bg); color: var(--success); }
.result-type { font-size: .75rem; font-weight: 600; text-transform: uppercase; letter-spacing: .08em; color: var(--gray-400); }
.result-nom  { font-size: 1.15rem; font-weight: 800; color: var(--gray-900); margin-top: 2px; }
.result-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px; }
.result-field { background: var(--gray-50); border-radius: var(--radius-sm); padding: 8px 12px; }
.field-label { display: block; font-size: .7rem; font-weight: 600; text-transform: uppercase; letter-spacing: .06em; color: var(--gray-400); margin-bottom: 3px; }
.field-value { font-size: .85rem; font-weight: 600; color: var(--gray-800); }
.stock-total {
  display: flex; align-items: center; gap: 10px;
  border-radius: var(--radius-sm); padding: 12px 16px; margin-bottom: 16px;
  font-size: .9rem; font-weight: 600;
}
.stock-ok   { background: var(--success-bg); color: var(--success); border: 1px solid #86efac; }
.stock-vide { background: var(--danger-bg);  color: var(--danger);  border: 1px solid #fca5a5; }
.section-subtitle { font-size: .84rem; font-weight: 700; color: var(--gray-700); margin: 14px 0 8px; display: flex; align-items: center; }
.stock-detail-list { display: flex; flex-direction: column; gap: 8px; }
.stock-detail-item {
  display: flex; justify-content: space-between; align-items: center;
  background: var(--gray-50); border: 1px solid var(--gray-200);
  border-radius: var(--radius-sm); padding: 10px 12px;
}
.stock-critique { background: #fff5f5; border-color: #fca5a5; }
.detail-entrepot { font-size: .85rem; font-weight: 700; color: var(--gray-900); }
.detail-zone { font-size: .78rem; color: var(--gray-500); }
.detail-emp {
  display: inline-flex; align-items: center; gap: 3px;
  font-size: .73rem; background: var(--navy-xlight); color: var(--navy);
  padding: 2px 6px; border-radius: 4px; margin-top: 3px;
}
.detail-right { display: flex; gap: 10px; flex-shrink: 0; }
.qty-block { text-align: center; }
.qty-label { display: block; font-size: .65rem; color: var(--gray-400); text-transform: uppercase; font-weight: 600; }
.qty-val   { display: block; font-size: 1.1rem; font-weight: 800; }
.qty-ok       { color: var(--success); }
.qty-zero     { color: var(--danger); }
.qty-reserved { color: var(--warning); }
.qty-transit  { color: #7c3aed; }
.qty-min      { color: var(--gray-500); }

/* ── Résultat LOCATION ── */
.hierarchy-path {
  display: flex; flex-wrap: wrap; align-items: center; gap: 4px;
  background: var(--gray-50); border: 1px solid var(--gray-200);
  border-radius: var(--radius-sm); padding: 10px 14px; margin-bottom: 14px;
  font-size: .82rem;
}
.hier-item     { color: var(--gray-600); }
.hier-sep      { color: var(--gray-300); }
.hier-current  { color: var(--navy); background: var(--navy-xlight); padding: 2px 8px; border-radius: 4px; }
.capacite-block { margin-bottom: 16px; }
.produit-list { display: flex; flex-direction: column; gap: 8px; }
.produit-item {
  display: flex; justify-content: space-between; align-items: center;
  background: var(--gray-50); border: 1px solid var(--gray-200);
  border-radius: var(--radius-sm); padding: 10px 12px;
}
.produit-nom { font-size: .85rem; font-weight: 700; color: var(--gray-900); }
.produit-ref { font-size: .75rem; font-family: monospace; color: var(--gray-500); }
.produit-qty { text-align: center; min-width: 60px; display: flex; flex-direction: column; align-items: center; }
.qty-big { font-size: 1.4rem; font-weight: 800; }
.qty-crit   { color: var(--danger); }
.qty-faible { color: var(--warning); }

/* ── Route /scan : redirection téléphone ── */
@media (max-width: 768px) {
  .gen-layout, .scan-layout { grid-template-columns: 1fr; }
  .qr-img { width: 200px; height: 200px; }
}
</style>
