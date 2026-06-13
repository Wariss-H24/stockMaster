# StockMaster — Documentation technique

## Table des matières

1. [Vue d'ensemble du projet](#1-vue-densemble-du-projet)
2. [Architecture générale](#2-architecture-générale)
3. [Flux de données — comment tout se connecte](#3-flux-de-données--comment-tout-se-connecte)
4. [Backend — Spring Boot](#4-backend--spring-boot)
   - [Structure des dossiers](#41-structure-des-dossiers)
   - [Couche Entity](#42-couche-entity)
   - [Couche Repository](#43-couche-repository)
   - [Couche DTO](#44-couche-dto)
   - [Couche Mapper](#45-couche-mapper)
   - [Couche Service](#46-couche-service)
   - [Couche Controller](#47-couche-controller)
   - [Sécurité JWT](#48-sécurité-jwt)
   - [Gestion des exceptions](#49-gestion-des-exceptions)
   - [Bootstrap — DataLoader](#410-bootstrap--dataloader)
   - [Configuration](#411-configuration)
5. [Frontend — Vue 3](#5-frontend--vue-3)
   - [Structure des dossiers](#51-structure-des-dossiers)
   - [main.js](#52-mainjs)
   - [App.vue](#53-appvue)
   - [Router](#54-router)
   - [Services](#55-services)
   - [Vues](#56-vues)
   - [Composants](#57-composants)
6. [Modules implémentés](#6-modules-implémentés)
7. [Système d'authentification complet](#7-système-dauthentification-complet)
8. [Permissions par rôle](#8-permissions-par-rôle)
9. [Commandes pour démarrer](#9-commandes-pour-démarrer)
10. [Comptes de démonstration](#10-comptes-de-démonstration)

---

## 1. Vue d'ensemble du projet

**StockMaster** est une application de gestion des stocks en temps réel pour une entreprise possédant plusieurs entrepôts.

| Élément | Technologie |
|---|---|
| Backend | Java 21 + Spring Boot 3.2.5 |
| Base de données (dev) | H2 in-memory |
| Base de données (prod) | MySQL |
| Sécurité | Spring Security + JWT (JJWT 0.11.5) |
| Hachage mots de passe | BCrypt |
| Frontend | Vue 3 + Vite 5 |
| Communication | REST API + Axios |
| Build backend | Maven |

---

## 2. Architecture générale

```
┌─────────────────────────────────────────────────────────┐
│                     NAVIGATEUR                          │
│                                                         │
│   Vue 3 (port 3000)                                     │
│   ┌──────────┐  ┌──────────┐  ┌──────────────────────┐ │
│   │  Router  │  │authStore │  │   Vues / Composants  │ │
│   │  Guards  │  │(JWT local│  │  (pages CRUD)        │ │
│   └──────────┘  │ storage) │  └──────────────────────┘ │
│                 └──────────┘                            │
│                      │  Axios + Bearer Token            │
└──────────────────────┼──────────────────────────────────┘
                        │ HTTP/REST (proxy Vite → 8080)
┌──────────────────────┼──────────────────────────────────┐
│                     BACKEND                             │
│                                                         │
│   Spring Boot (port 8080)                               │
│                                                         │
│   JwtFilter → SecurityConfig → Controller               │
│                                    │                    │
│                                 Service                 │
│                                    │                    │
│                           Mapper  DTO                   │
│                                    │                    │
│                               Repository                │
│                                    │                    │
│                              H2 / MySQL                 │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Flux de données — comment tout se connecte

### Connexion (Login)

```
1. Utilisateur saisit username + motDePasse dans LoginPage.vue
2. authApi.login({ username, motDePasse }) → POST /api/auth/login
3. JwtFilter laisse passer (route publique)
4. AuthController.login() reçoit LoginRequest
5. AuthService.login() :
   a. authManager.authenticate() → compare motDePasse avec hash BCrypt en base
   b. Si OK → JwtUtil.genererToken(username) → token JWT signé HS256
   c. Retourne AuthResponse { token, username, nomComplet, roles }
6. Frontend : authStore.login(token, user) → sauvegarde dans localStorage
7. Router redirige vers /tableau-de-bord
```

### Requête protégée (ex: GET /api/produits)

```
1. produitApi.findAll() → GET /api/produits
2. Intercepteur Axios : ajoute header "Authorization: Bearer <token>"
3. JwtFilter.doFilterInternal() :
   a. Extrait le token du header
   b. JwtUtil.estValide(token) → vérifie signature + expiration
   c. JwtUtil.extraireUsername(token) → "admin"
   d. UserDetailsServiceImpl.loadUserByUsername("admin") → charge depuis BDD
   e. Injecte UsernamePasswordAuthenticationToken dans SecurityContext
4. SecurityConfig vérifie le rôle (ex: GET → tous les rôles OK)
5. ProduitController.tous() → ProduitService.findAll() → ProduitRepository
6. ProduitMapper.toDTO() → retourne List<ProduitDTO> en JSON
7. Axios reçoit la réponse → la vue affiche les données
```

### Création d'un utilisateur par l'admin

```
1. Admin remplit le formulaire dans UtilisateursPage.vue
2. Validation frontend : username, nomComplet, motDePasse obligatoires
3. utilisateurApi.creer(payload) → POST /api/utilisateurs
4. JwtFilter valide le token admin
5. SecurityConfig vérifie ROLE_ADMIN pour POST /api/utilisateurs
6. UtilisateurController.creer() reçoit UtilisateurDTO
7. UtilisateurService.creer() :
   a. Vérifie que le username n'existe pas déjà
   b. passwordEncoder.encode(motDePasse) → hash BCrypt
   c. Associe le rôle demandé depuis RoleRepository
   d. repo.save(utilisateur) → stocke en base
8. L'utilisateur créé peut maintenant se connecter avec son mot de passe
```

### Déconnexion

```
1. Clic sur le bouton déconnexion dans la sidebar
2. authStore.logout() :
   a. token = null
   b. user = null
   c. localStorage.removeItem('sm_token')
   d. localStorage.removeItem('sm_user')
3. Router pousse vers /login
4. Guard router : toute navigation vers route protégée → redirigé /login
```

---

## 4. Backend — Spring Boot

### 4.1 Structure des dossiers

```
backend/src/main/java/maker/backend/
│
├── bootstrap/          → Données initiales au démarrage
├── config/             → Sécurité, JWT (filtre, utilitaire)
├── controller/         → Points d'entrée REST (HTTP)
├── dto/                → Objets de transfert (ce qui circule sur le réseau)
├── entity/             → Modèles JPA (tables en base)
├── exception/          → Gestion centralisée des erreurs
├── mapper/             → Conversion Entity ↔ DTO
├── repository/         → Accès base de données (Spring Data JPA)
├── service/            → Logique métier
└── BackendApplication  → Point d'entrée Spring Boot
```

---

### 4.2 Couche Entity

> Les entités représentent les **tables en base de données**. Hibernate les gère automatiquement.

#### `Utilisateur.java`
Table `utilisateurs`. Représente un compte utilisateur.

| Champ | Type | Description |
|---|---|---|
| id | Long | Clé primaire auto-incrémentée |
| username | String | Nom d'utilisateur unique |
| motDePasse | String | Mot de passe hashé BCrypt |
| nomComplet | String | Nom affiché |
| actif | boolean | Compte activé ou désactivé (suppression logique) |
| roles | Set\<Role\> | Relation ManyToMany → table `utilisateur_roles` |

#### `Role.java`
Table `roles`. Valeurs : `ADMIN`, `GESTIONNAIRE`, `MAGASINIER`, `AUDITEUR`.

| Champ | Type | Description |
|---|---|---|
| id | Long | Clé primaire |
| nom | String | Nom du rôle (unique) |

#### `Entrepot.java`
Table `entrepots`. Représente un site physique de stockage.

| Champ | Type | Description |
|---|---|---|
| id | Long | Clé primaire |
| nom | String | Nom de l'entrepôt (ex: ENT-Paris) |
| adresse | String | Adresse physique |
| responsable | String | Nom du responsable |
| capaciteTotale | Integer | Capacité max (unités arbitraires) |
| capaciteUtilisee | Integer | Capacité actuellement occupée |
| actif | boolean | Désactivation logique |

#### `ZoneFr.java`
Table `zones`. Subdivision d'un entrepôt (Réception, Stockage, Expédition...).

| Champ | Type | Description |
|---|---|---|
| id | Long | Clé primaire |
| nom | String | Nom de la zone |
| entrepot | Entrepot | Relation ManyToOne → entrepôt parent |
| capaciteTotale | Integer | Capacité de la zone |
| capaciteUtilisee | Integer | Occupation actuelle |
| actif | boolean | Désactivation logique |

#### `Produit.java`
Table `produits`. Article géré dans le stock.

| Champ | Type | Description |
|---|---|---|
| id | Long | Clé primaire |
| reference | String | Référence unique (ex: PRD-001) |
| codeBarre | String | Code-barres EAN |
| nom | String | Nom du produit |
| categorie | String | Catégorie libre (Informatique, etc.) |
| description | String | Description courte |
| prixAchat | Double | Prix d'achat HT |
| prixVente | Double | Prix de vente HT |
| poids | Double | Poids en kg |
| volume | Double | Volume en m³ |
| supprime | boolean | Suppression logique (jamais effacé physiquement) |

---

### 4.3 Couche Repository

> Interfaces Spring Data JPA. Spring génère automatiquement le SQL à partir des noms de méthodes.

| Fichier | Méthodes personnalisées |
|---|---|
| `UtilisateurRepository` | `findByUsername(String)` |
| `RoleRepository` | `findByNom(String)` |
| `EntrepotRepository` | `findByActifTrue()` |
| `ZoneFrRepository` | `findByActifTrue()`, `findByEntrepotIdAndActifTrue(Long)` |
| `ProduitRepository` | `findBySupprimeFalse()`, `findByReferenceAndSupprimeFalse(String)` |

**Pourquoi deux fichiers legacy (ProductRepository, UserRepository, RoleRepositoryFr) ?**
Ce sont d'anciens fichiers conservés vides avec `@Deprecated` pour ne pas casser la compilation. Ils ne sont plus utilisés.

---

### 4.4 Couche DTO

> Les DTOs (Data Transfer Objects) définissent **ce qui transite sur le réseau**. Ils protègent les entités JPA de l'exposition directe.

| Fichier | Rôle |
|---|---|
| `UtilisateurDTO` | Données utilisateur — le `motDePasse` n'est **jamais** renvoyé dans les réponses |
| `EntrepotDTO` | Données entrepôt + champ calculé `tauxOccupation` |
| `ZoneFrDTO` | Données zone + `entrepotId` (reçu) + `entrepotNom` (renvoyé) |
| `ProduitDTO` | Tous les champs du module 4 |
| `LoginRequest` | `{ username, motDePasse }` reçu au login |
| `RegisterRequest` | `{ username, motDePasse, nomComplet }` reçu à l'inscription |
| `AuthResponse` | `{ token, username, nomComplet, roles }` renvoyé après auth |

---

### 4.5 Couche Mapper

> Convertit manuellement Entity → DTO et DTO → Entity. Évite d'exposer les entités Hibernate directement.

| Fichier | Logique spéciale |
|---|---|
| `UtilisateurMapper` | `toDTO()` n'inclut pas `motDePasse` (sécurité) |
| `EntrepotMapper` | Calcule `tauxOccupation = (capaciteUtilisee / capaciteTotale) * 100` |
| `ZoneFrMapper` | Mappe `entrepot.id` → `entrepotId` et `entrepot.nom` → `entrepotNom` |
| `ProduitMapper` | Force `supprime = false` à la création |

---

### 4.6 Couche Service

> Contient toute la **logique métier**. Les controllers ne font qu'appeler les services.

#### `UtilisateurService`
- `findAll()` → retourne tous les utilisateurs (sans mot de passe)
- `creer()` → vérifie unicité username + **encode BCrypt** le mot de passe + associe les rôles
- `modifier()` → met à jour les champs + **re-encode** si nouveau mot de passe fourni
- `desactiver()` → passe `actif = false` (jamais de suppression physique)

#### `EntrepotService`
- `findAll()` → uniquement les entrepôts `actif = true`
- `creer()` / `modifier()` → CRUD standard
- `desactiver()` → suppression logique

#### `ZoneFrService`
- `findAll()` → uniquement les zones `actif = true`
- `creer()` → résout l'entrepôt parent depuis `entrepotId`
- `desactiver()` → suppression logique

#### `ProduitService`
- `findAll()` → uniquement les produits `supprime = false`
- `supprimerLogique()` → passe `supprime = true` (le produit reste en base pour la traçabilité)

#### `AuthService`
- `login()` → délègue à `AuthenticationManager` (qui utilise BCrypt) + génère JWT
- `register()` → encode BCrypt + rôle `MAGASINIER` par défaut + génère JWT

#### `UserDetailsServiceImpl`
- Implémentation de `UserDetailsService` pour Spring Security
- Charge l'utilisateur depuis la base + convertit ses rôles en `GrantedAuthority` avec préfixe `ROLE_`

---

### 4.7 Couche Controller

> Reçoit les requêtes HTTP, appelle le service, retourne la réponse. Logique minimale.

| Controller | Route de base | Rôles requis |
|---|---|---|
| `AuthController` | `/api/auth` | Public |
| `UtilisateurController` | `/api/utilisateurs` | ADMIN |
| `EntrepotController` | `/api/entrepots` | GET: tous / POST-PUT: ADMIN+GEST / DELETE: ADMIN |
| `ZoneFrController` | `/api/zones` | Idem entrepôts |
| `ProduitController` | `/api/produits` | Idem entrepôts |

Chaque controller utilise :
- `@Valid` pour déclencher la validation Bean Validation
- `@CrossOrigin(origins = "http://localhost:3000")` pour autoriser le frontend
- `ResponseEntity` pour contrôler le code HTTP retourné (201 Created, 204 No Content...)

---

### 4.8 Sécurité JWT

#### `JwtUtil.java`
Utilitaire qui gère les tokens JWT.

```
genererToken(username)  → crée un JWT signé HS256, valide 24h
extraireUsername(token) → lit le sujet (username) du token
estValide(token)        → vérifie signature + expiration
```

La clé secrète est lue depuis `application.properties` (`jwt.secret`). Elle est convertie en clé HMAC-SHA256 via `Keys.hmacShaKeyFor()`.

#### `JwtFilter.java`
Filtre Spring exécuté à **chaque requête HTTP**.

```
1. Lit le header "Authorization"
2. Si commence par "Bearer " → extrait le token
3. JwtUtil.estValide(token) → si OK :
   a. Extrait le username
   b. Charge UserDetails depuis la base
   c. Crée UsernamePasswordAuthenticationToken
   d. L'injecte dans SecurityContextHolder
4. Laisse passer la requête
```

#### `SecurityConfig.java`
Définit les règles d'accès globales :

```
/api/auth/**          → public (login, register)
/h2-console/**        → public (dev)
/api/utilisateurs/**  → ROLE_ADMIN seulement
GET /api/**           → tous les rôles connectés
POST/PUT /api/**      → ROLE_ADMIN, ROLE_GESTIONNAIRE
DELETE /api/**        → ROLE_ADMIN seulement
```

Paramètres clés :
- `CSRF désactivé` → l'API REST est stateless, pas de session
- `SessionCreationPolicy.STATELESS` → Spring ne crée aucune session HTTP
- `BCryptPasswordEncoder` → bean partagé pour encoder/vérifier les mots de passe

---

### 4.9 Gestion des exceptions

#### `ResourceNotFoundException`
Exception personnalisée levée quand une ressource est introuvable en base.
→ Traduite automatiquement en **HTTP 404**.

#### `GlobalExceptionHandler`
Intercepte toutes les exceptions et retourne du JSON structuré :

```json
// 404 - Ressource introuvable
{ "status": 404, "message": "Produit introuvable : 99", "timestamp": "..." }

// 400 - Validation échouée (@NotBlank, @Min...)
{ "status": 400, "erreurs": { "nom": "Le nom est obligatoire" }, "timestamp": "..." }

// 400 - Erreur métier (username déjà pris, identifiants incorrects...)
{ "status": 400, "message": "Ce nom d'utilisateur est déjà pris", "timestamp": "..." }
```

---

### 4.10 Bootstrap — DataLoader

Exécuté **une seule fois** au démarrage si les tables sont vides.

Crée automatiquement :
- 4 rôles : `ADMIN`, `GESTIONNAIRE`, `MAGASINIER`, `AUDITEUR`
- 4 utilisateurs de démonstration (mots de passe hashés BCrypt)
- 2 entrepôts : ENT-Paris, ENT-Lyon
- 3 zones dans ENT-Paris
- 4 produits de démonstration

---

### 4.11 Configuration

#### `application.properties`
```properties
# Base H2 in-memory (dev)
spring.datasource.url=jdbc:h2:mem:stockdb
spring.jpa.hibernate.ddl-auto=update   # Crée/met à jour les tables automatiquement
spring.h2.console.enabled=true          # Interface web H2 sur /h2-console

# JWT
jwt.secret=StockMasterSecretKey...      # Clé de signature (min 256 bits)
jwt.expiration=86400000                 # 24 heures en millisecondes
```

#### `pom.xml` — Dépendances clés

| Dépendance | Rôle |
|---|---|
| `spring-boot-starter-web` | Serveur Tomcat + REST MVC |
| `spring-boot-starter-data-jpa` | Hibernate + Spring Data |
| `spring-boot-starter-security` | Authentification + autorisation |
| `spring-boot-starter-validation` | @NotBlank, @Min, @Valid |
| `jjwt-api/impl/jackson` | Génération et validation JWT |
| `h2` | Base in-memory pour développement |
| `mysql-connector-j` | Connecteur MySQL pour production |
| `lombok` | Génère getters/setters/constructeurs |

---

## 5. Frontend — Vue 3

### 5.1 Structure des dossiers

```
frontend/src/
│
├── components/         → Composants réutilisables
│   └── ConfirmModal.vue
│
├── router/             → Navigation et guards d'accès
│   └── index.js
│
├── services/           → Communication API + état global
│   ├── api.js          → Toutes les requêtes HTTP (Axios)
│   └── authStore.js    → État d'authentification (token, user)
│
├── views/              → Pages complètes (une par route)
│   ├── LoginPage.vue
│   ├── TableauDeBord.vue
│   ├── EntrepotsPage.vue
│   ├── ZonesPage.vue
│   ├── ProduitsPage.vue
│   └── UtilisateursPage.vue
│
├── App.vue             → Layout principal (sidebar + topbar)
└── main.js             → Point d'entrée Vue
```

---

### 5.2 `main.js`
Point d'entrée de l'application Vue.
```
createApp(App) → monte l'application sur #app
.use(router)   → active la navigation
.mount('#app')
```

---

### 5.3 `App.vue`
Layout principal de l'application. Contient :

- **Sidebar fixe** (navy) avec logo, liens de navigation, infos utilisateur connecté, bouton déconnexion
- **Topbar** (blanc) avec bouton burger (mobile), titre de la page courante, badge version
- **Overlay mobile** pour fermer la sidebar sur petit écran
- **Menu "Utilisateurs"** affiché uniquement si `authStore.aRole('ADMIN')`
- **Logique responsive** : sidebar cachée sur mobile, visible sur desktop

La sidebar disparaît complètement sur la page `/login` grâce à :
```vue
<router-view v-if="$route.meta.public" />  <!-- Login : pas de sidebar -->
<div v-else class="layout">...</div>        <!-- Autres pages : avec sidebar -->
```

---

### 5.4 Router

#### `index.js`
Définit toutes les routes et les **guards de navigation** :

```
/login              → LoginPage (public, pas de token requis)
/tableau-de-bord    → TableauDeBord (auth requise)
/entrepots          → EntrepotsPage (auth requise)
/zones              → ZonesPage (auth requise)
/produits           → ProduitsPage (auth requise)
/utilisateurs       → UtilisateursPage (auth requise + rôle ADMIN)
```

**Guard global `router.beforeEach()`** :
```
1. Route publique (/login) → laisse passer
2. Pas de token → redirige vers /login
3. Rôle insuffisant (ex: GESTIONNAIRE sur /utilisateurs) → redirige vers /tableau-de-bord
4. Sinon → laisse passer
```

---

### 5.5 Services

#### `authStore.js`
Store réactif global (sans Pinia ni Vuex) qui gère l'état d'authentification.

```javascript
authStore.token          // JWT en cours (ou null)
authStore.user           // { username, nomComplet, roles: [...] }
authStore.estConnecte    // true si token présent
authStore.aRole('ADMIN') // true si l'utilisateur a ce rôle
authStore.aUnRole('ADMIN', 'GESTIONNAIRE') // true si au moins un rôle correspond

authStore.login(token, user) // sauvegarde token + user dans localStorage
authStore.logout()           // efface tout de localStorage
```

La persistance via `localStorage` permet de rester connecté après un rechargement de page.

#### `api.js`
Instance Axios centrale avec deux intercepteurs :

**Intercepteur requête** → injecte automatiquement le token JWT :
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Intercepteur réponse** → déconnexion automatique si 401 (token expiré ou invalide) :
```
401 reçu → authStore.logout() → redirect /login
```

Expose les fonctions API par domaine :
```javascript
authApi        → login(), register()
utilisateurApi → findAll(), creer(), modifier(), desactiver()
entrepotApi    → findAll(), creer(), modifier(), desactiver()
zoneApi        → findAll(), creer(), modifier(), desactiver()
produitApi     → findAll(), creer(), modifier(), supprimer()
```

---

### 5.6 Vues

#### `LoginPage.vue`
Page d'authentification. Deux modes : **connexion** et **inscription**.

Fonctionnalités :
- Toggle show/hide mot de passe
- Validation côté client avant envoi
- Boutons de démo (remplissage automatique)
- Gestion des deux formats d'erreur backend (`{ message }` et `{ erreurs: {...} }`)
- Redirection vers `/tableau-de-bord` après succès

#### `TableauDeBord.vue`
Page d'accueil après connexion.

Affiche :
- 4 cartes KPI : nombre d'entrepôts, zones, produits, utilisateurs
- Barres de progression de l'occupation de chaque entrepôt
  - Vert si < 50%, Orange si < 80%, Rouge si ≥ 80%

#### `EntrepotsPage.vue`
CRUD complet des entrepôts.

- Tableau avec recherche live (nom, adresse, responsable)
- Barre de progression de l'occupation
- Modal formulaire (créer / éditer)
- Modal de confirmation désactivation (type `warning`)
- Boutons visibles selon le rôle :
  - **Éditer** : ADMIN + GESTIONNAIRE
  - **Désactiver** : ADMIN seulement

#### `ZonesPage.vue`
CRUD complet des zones.

- Sélecteur d'entrepôt parent dans le formulaire
- Badge avec le nom de l'entrepôt parent
- Mêmes règles de visibilité que les entrepôts

#### `ProduitsPage.vue`
CRUD complet du catalogue produits.

- Recherche live sur référence, nom, catégorie
- Calcul automatique de la marge commerciale :
  ```
  marge = ((prixVente - prixAchat) / prixVente) * 100
  ```
  - Vert ≥ 20%, Orange ≥ 10%, Rouge < 10%
- Modal de confirmation suppression (type `danger`)
- Suppression logique uniquement (le produit reste en base)

#### `UtilisateursPage.vue`
CRUD des utilisateurs (ADMIN seulement).

- Avatar avec initiales générées automatiquement
- Validation stricte : mot de passe obligatoire à la création
- Empêche l'admin de se désactiver lui-même (`u.username !== moi`)
- Les rôles sont mis à jour à chaque modification

---

### 5.7 Composants

#### `ConfirmModal.vue`
Modale de confirmation réutilisable avec 3 variantes visuelles.

| Prop | Type | Valeurs | Description |
|---|---|---|---|
| `titre` | String | - | Titre de la modale |
| `message` | String | - | Texte explicatif avec le nom de la ressource |
| `type` | String | `danger`, `warning`, `info` | Couleur et icône |
| `labelConfirmer` | String | - | Texte du bouton de confirmation |

Événements émis : `@confirmer`, `@annuler`

Exemples d'utilisation :
- Suppression produit → `type="danger"`, icône corbeille, bouton rouge
- Désactivation entrepôt/zone/utilisateur → `type="warning"`, icône croix, bouton orange

---

## 6. Modules implémentés

| Module | Statut | Détail |
|---|---|---|
| **Module 1** — Gestion des utilisateurs | ✅ Complet | CRUD, rôles, activation/désactivation |
| **Module 2** — Gestion des entrepôts | ✅ Complet | CRUD, capacité, taux d'occupation |
| **Module 3** — Gestion des zones | ✅ Complet | CRUD, liaison entrepôt, occupation |
| **Module 4** — Gestion des produits | ✅ Complet | CRUD, suppression logique, marge |
| **Authentification JWT** | ✅ Complet | Login, register, token, déconnexion |
| Modules 5 à 18 | 🔜 À venir | Catégories, fournisseurs, stocks... |

---

## 7. Système d'authentification complet

```
┌─────────────────────────────────────────────────────────────┐
│                    CYCLE DE VIE DU TOKEN                    │
│                                                             │
│  Login/Register                                             │
│       │                                                     │
│       ▼                                                     │
│  AuthService.login()                                        │
│       │  BCrypt.matches(mdpBrut, hashEnBase)                │
│       │  ✅ OK → JwtUtil.genererToken(username)             │
│       │                                                     │
│       ▼                                                     │
│  AuthResponse { token, username, nomComplet, roles }        │
│       │                                                     │
│       ▼                                                     │
│  authStore.login() → localStorage                           │
│       │                                                     │
│       ▼                                                     │
│  Axios intercepteur → "Authorization: Bearer <token>"       │
│  sur chaque requête suivante                                │
│       │                                                     │
│       ▼                                                     │
│  JwtFilter → valide → injecte dans SecurityContext          │
│       │                                                     │
│       ▼                                                     │
│  SecurityConfig → vérifie les rôles → autorise/refuse       │
│                                                             │
│  Token expiré (24h) → 401 → authStore.logout() → /login    │
└─────────────────────────────────────────────────────────────┘
```

---

## 8. Permissions par rôle

| Action | ADMIN | GESTIONNAIRE | MAGASINIER | AUDITEUR |
|---|:---:|:---:|:---:|:---:|
| Voir tableau de bord | ✅ | ✅ | ✅ | ✅ |
| Voir entrepôts | ✅ | ✅ | ✅ | ✅ |
| Créer / modifier entrepôt | ✅ | ✅ | ❌ | ❌ |
| Désactiver entrepôt | ✅ | ❌ | ❌ | ❌ |
| Voir zones | ✅ | ✅ | ✅ | ✅ |
| Créer / modifier zone | ✅ | ✅ | ❌ | ❌ |
| Désactiver zone | ✅ | ❌ | ❌ | ❌ |
| Voir produits | ✅ | ✅ | ✅ | ✅ |
| Créer / modifier produit | ✅ | ✅ | ❌ | ❌ |
| Supprimer produit | ✅ | ❌ | ❌ | ❌ |
| Accéder à la page Utilisateurs | ✅ | ❌ | ❌ | ❌ |
| Créer / modifier utilisateur | ✅ | ❌ | ❌ | ❌ |
| Désactiver utilisateur | ✅ | ❌ | ❌ | ❌ |

**Règles appliquées aux deux niveaux :**
- **Backend** : `SecurityConfig` bloque les requêtes non autorisées → retourne HTTP 403
- **Frontend** : boutons et menus masqués via `v-if` + `authStore.aRole()`

---

## 9. Commandes pour démarrer

### Backend
```bash
# Première fois ou après modification
cd backend
mvn clean package -DskipTests

# Lancer le serveur (port 8080)
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

Console H2 accessible sur : `http://localhost:8080/h2-console`
- JDBC URL : `jdbc:h2:mem:stockdb`
- Username : `sa` / Password : *(vide)*

### Frontend
```bash
# Installer les dépendances (une seule fois)
cd frontend
npm install

# Lancer en développement (port 3000)
npm run dev
```

Application accessible sur : `http://localhost:3000`

Le proxy Vite redirige automatiquement `/api/*` → `http://localhost:8080/api/*`.

---

## 10. Comptes de démonstration

| Username | Mot de passe | Rôle | Accès |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Accès total |
| `gestionnaire` | `gest123` | GESTIONNAIRE | Lecture + création/modification |
| `magasinier` | `mag123` | MAGASINIER | Lecture seule |
| `auditeur` | `audit123` | AUDITEUR | Lecture seule |

> Les mots de passe sont hashés en BCrypt en base. Jamais stockés en clair.
