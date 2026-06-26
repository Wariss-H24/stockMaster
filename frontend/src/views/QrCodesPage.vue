<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Codes-barres & QR Codes</h1>
      <p class="page-subtitle">Module 18 — Génération QR Code produits et emplacements</p>
    </div>

    <div style="display:grid;grid-template-columns:1fr 1fr;gap:24px;">

      <!-- QR Produits -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">QR Code Produit</h2>
        </div>
        <div class="card-body">
          <div class="form-group">
            <label class="form-label">Sélectionner un produit</label>
            <select class="form-select" v-model.number="produitSelectionne" @change="genererQrProduit">
              <option value="">Sélectionner</option>
              <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.reference }} — {{ p.nom }}</option>
            </select>
          </div>
          <div v-if="chargementProduit" class="empty-state">Génération en cours...</div>
          <div v-else-if="qrProduit" class="qr-result">
            <img :src="'data:image/png;base64,' + qrProduit" alt="QR Produit" class="qr-img" />
            <p class="qr-label">{{ produitNom }}</p>
            <a :href="'data:image/png;base64,' + qrProduit" :download="'qr-produit-' + produitSelectionne + '.png'" class="btn btn-primary btn-sm">
              Télécharger PNG
            </a>
          </div>
          <div v-else class="empty-state">Sélectionnez un produit pour générer son QR Code.</div>
        </div>
      </div>

      <!-- QR Emplacements -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">QR Code Emplacement</h2>
        </div>
        <div class="card-body">
          <div class="form-group">
            <label class="form-label">Sélectionner un emplacement</label>
            <select class="form-select" v-model.number="emplacementSelectionne" @change="genererQrEmplacement">
              <option value="">Sélectionner</option>
              <option v-for="e in emplacements" :key="e.id" :value="e.id">{{ e.codeComplet }}</option>
            </select>
          </div>
          <div v-if="chargementEmplacement" class="empty-state">Génération en cours...</div>
          <div v-else-if="qrEmplacement" class="qr-result">
            <img :src="'data:image/png;base64,' + qrEmplacement" alt="QR Emplacement" class="qr-img" />
            <p class="qr-label">{{ emplacementCode }}</p>
            <a :href="'data:image/png;base64,' + qrEmplacement" :download="'qr-emplacement-' + emplacementSelectionne + '.png'" class="btn btn-primary btn-sm">
              Télécharger PNG
            </a>
          </div>
          <div v-else class="empty-state">Sélectionnez un emplacement pour générer son QR Code.</div>
        </div>
      </div>

    </div>

    <!-- Impression en lot -->
    <div class="card" style="margin-top:24px;">
      <div class="card-header">
        <h2 class="card-title">Impression en lot</h2>
      </div>
      <div class="card-body">
        <p style="font-size:.88rem;color:var(--gray-500);margin-bottom:16px;">Sélectionnez plusieurs produits pour imprimer leurs QR codes.</p>
        <div class="lot-grid">
          <label v-for="p in produits" :key="p.id" class="lot-item">
            <input type="checkbox" :value="p.id" v-model="selectionLot" />
            <span>{{ p.reference }} — {{ p.nom }}</span>
          </label>
        </div>
        <div style="margin-top:16px;display:flex;gap:10px;align-items:center;">
          <button class="btn btn-primary" @click="genererLot" :disabled="selectionLot.length === 0 || chargementLot">
            {{ chargementLot ? 'Génération...' : `Générer ${selectionLot.length} QR Code(s)` }}
          </button>
          <button class="btn btn-outline" @click="selectionLot = []">Tout désélectionner</button>
        </div>
        <div v-if="qrLot.length > 0" class="qr-lot-result">
          <div v-for="item in qrLot" :key="item.id" class="qr-lot-item">
            <img :src="'data:image/png;base64,' + item.base64" :alt="item.nom" class="qr-img-sm" />
            <p class="qr-label-sm">{{ item.reference }}</p>
            <a :href="'data:image/png;base64,' + item.base64" :download="'qr-' + item.reference + '.png'" class="btn btn-outline btn-sm" style="font-size:.75rem;">DL</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { produitApi, emplacementApi, qrCodeApi } from '../services/api.js'

export default {
  name: 'QrCodesPage',
  data() {
    return {
      produits: [], emplacements: [],
      produitSelectionne: '', emplacementSelectionne: '',
      qrProduit: null, qrEmplacement: null,
      produitNom: '', emplacementCode: '',
      chargementProduit: false, chargementEmplacement: false,
      selectionLot: [], qrLot: [], chargementLot: false
    }
  },
  async mounted() {
    try {
      const p = await produitApi.findAll()
      this.produits = p.data
    } catch (e) { console.error('Erreur chargement produits', e) }
    try {
      const e = await emplacementApi.findAll()
      this.emplacements = e.data
    } catch (e) { console.error('Erreur chargement emplacements', e) }
  },
  methods: {
    async genererQrProduit() {
      if (!this.produitSelectionne) return
      this.chargementProduit = true
      this.qrProduit = null
      try {
        const res = await qrCodeApi.produit(this.produitSelectionne)
        this.qrProduit = res.data.base64
        const p = this.produits.find(p => p.id === this.produitSelectionne)
        this.produitNom = p ? `${p.reference} — ${p.nom}` : ''
      } finally { this.chargementProduit = false }
    },
    async genererQrEmplacement() {
      if (!this.emplacementSelectionne) return
      this.chargementEmplacement = true
      this.qrEmplacement = null
      try {
        const res = await qrCodeApi.emplacement(this.emplacementSelectionne)
        this.qrEmplacement = res.data.base64
        const e = this.emplacements.find(e => e.id === this.emplacementSelectionne)
        this.emplacementCode = e ? e.codeComplet : ''
      } finally { this.chargementEmplacement = false }
    },
    async genererLot() {
      this.chargementLot = true
      this.qrLot = []
      try {
        const results = await Promise.all(
          this.selectionLot.map(async id => {
            const res = await qrCodeApi.produit(id)
            const p = this.produits.find(p => p.id === id)
            return { id, base64: res.data.base64, reference: p?.reference, nom: p?.nom }
          })
        )
        this.qrLot = results
      } finally { this.chargementLot = false }
    }
  }
}
</script>

<style scoped>
.card-header { padding:16px 20px;border-bottom:1px solid var(--gray-200); }
.card-title { font-size:1rem;font-weight:700;color:var(--gray-800); }
.card-body { padding:20px; }
.empty-state { text-align:center;padding:32px;color:var(--gray-400);font-size:.88rem; }
.qr-result { display:flex;flex-direction:column;align-items:center;gap:12px;padding:16px 0; }
.qr-img { width:220px;height:220px;border:1px solid var(--gray-200);border-radius:8px; }
.qr-img-sm { width:100px;height:100px;border:1px solid var(--gray-200);border-radius:6px; }
.qr-label { font-size:.82rem;color:var(--gray-600);font-family:monospace;text-align:center; }
.qr-label-sm { font-size:.75rem;color:var(--gray-600);font-family:monospace;text-align:center; }
.lot-grid { display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:8px; }
.lot-item { display:flex;align-items:center;gap:8px;padding:8px;border:1px solid var(--gray-200);border-radius:6px;cursor:pointer;font-size:.83rem; }
.lot-item:hover { background:var(--gray-50); }
.qr-lot-result { display:flex;flex-wrap:wrap;gap:16px;margin-top:20px;padding-top:20px;border-top:1px solid var(--gray-200); }
.qr-lot-item { display:flex;flex-direction:column;align-items:center;gap:6px; }
</style>
