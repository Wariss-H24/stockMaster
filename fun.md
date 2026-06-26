# StockMaster — Comment ça marche ?

> Ce document explique chaque fonctionnalité du projet, comment les pièces s'assemblent,
> et comment tu peux l'expliquer à quelqu'un d'autre avec confiance.

---

## La structure globale

Le projet est divisé en deux parties qui communiquent via HTTP :

```
Navigateur (Vue 3)          Backend (Spring Boot)
      │                            │
      │  ──── requête HTTP ────►  │  lit/écrit en base H2
      │  ◄─── réponse JSON ────   │
```

- **Frontend** : ce que l'utilisateur voit (pages Vue, formulaires, tableaux)
- **Backend** : le cerveau (règles métier, sécurité, base de données)
- **Base de données H2** : stockée en mémoire pendant le développement, recrée à chaque démarrage

---

## Comment le backend est organisé

Chaque fonctionnalité suit toujours le même chemin vertical :

```
Controller → Service → Repository → Base de données
```

| Couche | Rôle | Exemple |
|--------|------|---------|
| **Entity** | La structure d'une table en base | `Produit.java` = table `produits` |
| **Repository** | Les requêtes SQL (automatiques via JPA) | `produitRepo.findAll()` |
| **Service** | La logique métier (règles, calculs) | Vérifier le stock avant de sortir |
| **DTO** | Ce qu'on envoie/reçoit en JSON | `ProduitDTO` sans les champs internes |
| **Controller** | Écoute les requêtes HTTP et délègue au service | `GET /api/produits` |

**Exemple concret — créer un produit :**
1. Le frontend envoie `POST /api/produits` avec un JSON
2. `ProduitController.creer()` reçoit la requête
3. Il appelle `ProduitService.creer(dto)`
4. Le service convertit le DTO en entité et appelle `produitRepo.save(p)`
5. JPA écrit en base et retourne l'entité avec son ID
6. Le service reconvertit en DTO et le retourne au frontend

---

## Sécurité — JWT (JSON Web Token)

### Comment ça fonctionne

Quand tu te connectes avec `admin/admin123` :

1. Le backend vérifie le mot de passe (hashé avec BCrypt)
2. Il génère un **token JWT** — une chaîne signée qui contient ton username et tes rôles
3. Le frontend stocke ce token dans `localStorage`
4. À chaque requête suivante, le frontend ajoute `Authorization: Bearer <token>` dans l'en-tête
5. `JwtFilter.java` intercepte chaque requête, vérifie le token, et dit à Spring Security qui tu es

### Les rôles

| Rôle | Accès |
|------|-------|
| **ADMIN** | Tout : lire, créer, modifier, supprimer |
| **GESTIONNAIRE** | Lire + créer + modifier (pas supprimer) |
| **MAGASINIER** | Lire uniquement |
| **AUDITEUR** | Lire + accès traçabilité |

### Ce qui se passe si tu n'es pas connecté

`JwtFilter` ne trouve pas de token valide → Spring Security retourne **401 Unauthorized** → le frontend te redirige vers `/login`.

---

## Module 11 — Inventaires physiques

### Le problème que ça résout
En réalité, ce qu'il y a dans le système ne correspond pas toujours à ce qu'il y a physiquement dans l'entrepôt. L'inventaire permet de comparer et corriger.

### Comment ça marche

**Cycle de vie :** `EN_COURS → CLOTURÉ | ANNULÉ`

1. **Créer** : le système lit tous les stocks actuels de l'entrepôt et pré-remplit les lignes avec les quantités système
2. **Saisir** : le magasinier entre les quantités physiques réelles qu'il a comptées
3. **Écart** = `quantitePhysique - quantiteSysteme`
   - Positif (+3) → surplus → on ajoute 3 au stock
   - Négatif (-2) → manque → on retire 2 du stock
4. **Clôturer** : le système applique les ajustements et crée des mouvements de régularisation

### Ce qui se passe en base à la clôture

```java
// Pour chaque ligne avec un écart
stock.setQuantiteDisponible(stock.getQuantiteDisponible() + ecart);
// + un mouvement ENTREE ou SORTIE pour la traçabilité
```

---

## Module 12 — Alertes automatiques

### Le problème que ça résout
Tu ne veux pas surveiller manuellement tous les stocks. Tu veux être prévenu automatiquement quand un produit est en rupture imminente.

### Comment ça marche

**stockMin** : un seuil configuré sur chaque stock. Si `quantiteDisponible < stockMin` → alerte.

**Deux niveaux :**
- `FAIBLE` : dispo ≤ stockMin
- `CRITIQUE` : dispo = 0 **ou** dispo ≤ stockMin ÷ 2

**Le scheduler :**
```java
@Scheduled(cron = "0 0 * * * *")  // toutes les heures
public void verifierStocksAutomatiquement() { ... }
```
Cette annotation dit à Spring Boot d'exécuter la méthode automatiquement chaque heure, sans que personne ne clique sur quoi que ce soit.

**Comment configurer stockMin :**
1. Aller dans **Produits** → éditer un produit → remplir "Stock minimum"
2. Ce seuil se propage automatiquement sur tous les stocks de ce produit
3. À chaque bon de réception validé, le seuil du produit est appliqué au stock créé

**Le badge rouge dans la sidebar :**
`App.vue` appelle `/api/alertes/critiques` toutes les 60 secondes et affiche le nombre.

---

## Module 13 — Tableau de bord

### Le problème que ça résout
Avoir une vue synthétique de tout le système en un coup d'œil.

### Comment ça marche

`DashboardController` expose `GET /api/dashboard/kpis`.

`DashboardService.getKpis()` fait plusieurs requêtes en base et construit un objet avec :

| KPI | Calcul |
|-----|--------|
| Entrepôts actifs | `entrepotRepo.findByActifTrue().size()` |
| Stocks critiques | Stocks où `quantiteDisponible ≤ stockMin` |
| Entrées du mois | Mouvements de type ENTREE depuis le 1er du mois |
| Valeur totale | Σ `quantiteDisponible × prixAchat` pour tous les stocks |

**Les graphiques** sont dessinés en SVG pur (pas de bibliothèque externe). Le composant Vue calcule les hauteurs des barres et les angles du donut à partir des données :

```javascript
barHeight(val, max) {
  return Math.max(4, Math.round((val / max) * 120)) // pixels
}
```

---

## Module 14 — Reporting & Exports

### Le problème que ça résout
Partager des données avec des personnes qui n'ont pas accès à l'application (comptable, direction).

### Comment ça marche

`ReportingController` expose 6 endpoints qui retournent des **fichiers binaires** (pas du JSON).

**PDF** — via la bibliothèque OpenPDF :
```java
Document doc = new Document(PageSize.A4.rotate());
PdfWriter.getInstance(doc, outputStream);
// construit un tableau ligne par ligne
```

**Excel** — via Apache POI :
```java
Workbook wb = new XSSFWorkbook();  // crée un fichier .xlsx
Sheet sheet = wb.createSheet("Stock");
// remplit les cellules
wb.write(outputStream);
```

**CSV** — format texte simple, séparé par des virgules. Compatible avec Excel, Python, tout.

**Côté frontend :**
```javascript
const res = await reportingApi.pdfStock()        // responseType: 'blob'
const blob = new Blob([res.data], { type: 'application/pdf' })
const url = URL.createObjectURL(blob)
const a = document.createElement('a')
a.href = url; a.download = 'rapport.pdf'; a.click()
```
Le navigateur crée un lien temporaire et simule un clic pour télécharger le fichier.

---

## Module 15 — Traçabilité & Audit

### Le problème que ça résout
Savoir **qui** a fait **quoi** et **quand**. Indispensable en entreprise pour la conformité.

### Comment ça marche — Spring AOP

AOP = **Aspect-Oriented Programming**. L'idée : au lieu de copier-coller du code de log dans chaque méthode, on crée un "aspect" qui s'exécute **autour** des méthodes sans les modifier.

```java
@Tracable(entite = "BonReception", action = "VALIDER")
public BonReceptionDTO valider(@PathVariable Long id) { ... }
```

Quand cette méthode est appelée, **avant de retourner la réponse**, `AuditAspect` s'exécute automatiquement :

```
Requête → Controller.valider() → [AuditAspect intercepte] → AuditService.enregistrer()
```

L'aspect enregistre dans `audit_logs` :
- Qui (username du token JWT)
- Quoi (entite + action)
- L'ID de la ressource modifiée
- La date et l'IP

**Pourquoi `@Tracable` ne marche pas sur les méthodes privées :** Spring AOP fonctionne avec des proxies. Il ne peut intercepter que les méthodes publiques appelées depuis l'extérieur de la classe.

---

## Module 16 — Commandes Fournisseurs

### Le problème que ça résout
Formaliser le processus d'achat : passer une commande, la suivre, la réceptionner et mettre le stock à jour automatiquement.

### Cycle de vie

```
BROUILLON → ENVOYEE → RECUE
    └──────────────────► ANNULEE
```

| Statut | Ce qui se passe |
|--------|-----------------|
| BROUILLON | La commande est en cours de rédaction, modifiable |
| ENVOYEE | Envoyée au fournisseur, plus modifiable |
| RECUE | Le stock est automatiquement mis à jour |
| ANNULEE | Annulée avant réception |

**À la réception**, pour chaque ligne de commande :
```java
stock.setQuantiteDisponible(stock.getQuantiteDisponible() + ligne.getQuantite());
MouvementStock m = new MouvementStock();
m.setType(MouvementStock.Type.ENTREE);
// enregistré pour la traçabilité
```

---

## Comment le frontend communique avec le backend

### api.js — le point central

Tout passe par un fichier `api.js` qui configure Axios :

```javascript
const api = axios.create({ baseURL: '/api' })

// Ajoute automatiquement le token JWT à chaque requête
api.interceptors.request.use(config => {
  if (authStore.token) config.headers.Authorization = `Bearer ${authStore.token}`
  return config
})

// Si le backend retourne 401 → déconnexion automatique
api.interceptors.response.use(res => res, err => {
  if (err.response?.status === 401) { authStore.logout(); window.location.href = '/login' }
  return Promise.reject(err)
})
```

### Le proxy Vite

Le frontend tourne sur le port 3000, le backend sur 8080. Pour éviter les erreurs CORS en développement, `vite.config.js` redirige les requêtes `/api/*` vers `localhost:8080`.

### authStore.js — la session

Sans Pinia ni Vuex, juste un objet réactif Vue :
```javascript
export const authStore = reactive({
  token: localStorage.getItem('sm_token') || null,
  user: ...,
  get estConnecte() { return !!this.token },
  aRole(role) { return this.user?.roles?.includes(role) }
})
```
`reactive()` fait que Vue met à jour l'interface partout où `authStore` est utilisé dès qu'une propriété change.

---

## Les patterns récurrents dans l'UI

### Toast notification
Chaque page a un système de notification temporaire :
```javascript
afficherToast(message, type = 'succes', duree = 5000) {
  this.toast = { visible: true, type, message, duree }
  setTimeout(() => { this.toast.visible = false }, duree)
}
```
Une barre de progression CSS anime le temps restant.

### Cycle modale → action → rechargement
```javascript
async sauvegarder() {
  // 1. Valider les données
  // 2. Appel API
  await api.creer(payload)
  // 3. Fermer la modale
  this.modal = false
  // 4. Recharger la liste
  await this.charger()
  // 5. Notifier
  this.afficherToast('Créé avec succès', 'succes')
}
```

### Computed pour le filtrage
Au lieu de filtrer à chaque frappe de touche avec des méthodes, Vue recalcule `listeFiltree` automatiquement quand `recherche` change :
```javascript
computed: {
  listeFiltree() {
    if (!this.recherche) return this.liste
    return this.liste.filter(item => item.nom.toLowerCase().includes(this.recherche.toLowerCase()))
  }
}
```

---

## Résumé — La logique en une page

```
┌─────────────────────────────────────────────────────────────────┐
│                        FLUX D'UNE ACTION                        │
│                                                                  │
│  Utilisateur clique                                              │
│       │                                                          │
│       ▼                                                          │
│  Vue valide les données côté frontend                           │
│       │                                                          │
│       ▼                                                          │
│  axios envoie HTTP + token JWT                                   │
│       │                                                          │
│       ▼                                                          │
│  JwtFilter vérifie le token                                      │
│       │                                                          │
│       ▼                                                          │
│  SecurityConfig vérifie le rôle                                  │
│       │                                                          │
│       ▼                                                          │
│  Controller reçoit la requête                                    │
│       │                                                          │
│       ▼                                                          │
│  AuditAspect intercepte (si @Tracable)                          │
│       │                                                          │
│       ▼                                                          │
│  Service exécute la logique métier                               │
│   - Valide les règles (stock suffisant ? statut correct ?)       │
│   - Met à jour les entités                                       │
│   - Enregistre les mouvements de stock                           │
│       │                                                          │
│       ▼                                                          │
│  Repository écrit en base via JPA/Hibernate                      │
│       │                                                          │
│       ▼                                                          │
│  Le DTO est retourné en JSON                                     │
│       │                                                          │
│       ▼                                                          │
│  Vue met à jour l'interface + toast                              │
└─────────────────────────────────────────────────────────────────┘
```

### Les 3 concepts clés à retenir

**1. Séparation des responsabilités**
Chaque couche a un rôle précis. Le Controller ne connait pas la base de données, le Repository ne connait pas les règles métier. Ça rend le code maintenable.

**2. JWT stateless**
Le backend ne garde aucune session en mémoire. Chaque requête est autonome et contient toutes les informations nécessaires dans le token. Ça permet de scaler horizontalement (plusieurs serveurs).

**3. Réactivité Vue**
Les données sont déclarées dans `data()`, les calculs dérivés dans `computed`, les effets de bord dans `methods`. Vue gère automatiquement les mises à jour de l'interface quand les données changent — tu ne manipules jamais le DOM directement.
