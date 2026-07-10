# CHANGELOG — Corrections & Améliorations StockMaster

Date : 2025  
Modules concernés : 1 → 9

---

## 1. DataLoader — Correction critique au démarrage

**Fichier :** `backend/src/main/java/maker/backend/bootstrap/DataLoader.java`

**Problème :**  
Le DataLoader appelait `p.setCategorie(String)` alors que l'entité `Produit` utilise désormais une relation `@ManyToOne` vers l'entité `Categorie`. Le serveur plantait au démarrage avec une `MethodNotFoundException`.

**Correction :**
- Ajout de `CategorieRepository` en paramètre du `CommandLineRunner`
- Création des 4 catégories de démonstration **avant** les produits :
  - Informatique
  - Électroménager
  - Pièces auto
  - Alimentaire
- Les produits de démo sont maintenant liés à leur catégorie via un objet `Categorie` (FK réelle) au lieu d'un `String`

---

## 2. Suppression de UserRepository.java obsolète

**Fichier supprimé :** `backend/src/main/java/maker/backend/repository/UserRepository.java`

**Problème :**  
Ce fichier contenait une interface vide annotée `@Deprecated` sans aucune utilité. Il pouvait créer de la confusion dans le projet.

**Correction :**
- Suppression du fichier
- Vérification qu'aucun autre fichier ne le référençait (confirmé)

---

## 3. Stock — Contrainte unique corrigée

**Fichier :** `backend/src/main/java/maker/backend/entity/Stock.java`

**Problème :**  
La contrainte unique était définie sur `(produit_id, entrepot_id)` uniquement. Cela empêchait d'avoir le même produit dans deux zones différentes du même entrepôt, ce qui est pourtant un cas d'usage normal.

**Avant :**
```java
@UniqueConstraint(columnNames = {"produit_id", "entrepot_id"})
```

**Après :**
```java
@UniqueConstraint(columnNames = {"produit_id", "entrepot_id", "zone_id"})
```

**Résultat :**
- Produit A / ENT-Paris / Zone A ✅
- Produit A / ENT-Paris / Zone B ✅
- Produit A / ENT-Paris / sans zone ✅

---

## 4. BonSortieService — Optimisation de appliquerSortie()

**Fichier :** `backend/src/main/java/maker/backend/service/BonSortieService.java`

**Problème :**  
La méthode `appliquerSortie()` convertissait inutilement les entités `SortieLigne` en `SortieLigneDTO` via `stream().map(this::toDTO)`avant de les retraiter, ce qui ajoutait une conversion inutile.

**Correction :**
- Itération directe sur `bon.getLignes()` (entités `SortieLigne`)
- Suppression de la conversion intermédiaire en DTO
- Ajout d'un import propre pour `SortieLigne` (suppression des références fully-qualified `maker.backend.entity.SortieLigne`)
- Amélioration du message d'erreur : affiche le **nom du produit** au lieu de son id

**Avant :**
```
Stock insuffisant pour le produit id=3 dans l'entrepôt ENT-Paris (quantité disponible = 0)
```

**Après :**
```
Stock insuffisant pour le produit 'Laptop Dell XPS' dans l'entrepôt ENT-Paris (quantité disponible = 0)
```

---

## 5. BonReceptionsPage & BonSortiesPage — Suppression des alert()

**Fichiers :**
- `frontend/src/views/BonReceptionsPage.vue`
- `frontend/src/views/BonSortiesPage.vue`

**Problème :**  
Les méthodes `valider()` et `confirmerSuppression()` utilisaient `alert()` natif du navigateur pour afficher les erreurs, ce qui est bloquant et incohérent avec le reste de l'UI.

**Correction :**
- Remplacement de tous les `alert()` par `this.erreur = ...`
- Les erreurs s'affichent maintenant dans le bloc `<div class="form-error">` rouge déjà présent dans les pages

---

## 6. formaterDate — Sécurisation contre les dates nulles

**Fichiers :**
- `frontend/src/views/BonReceptionsPage.vue`
- `frontend/src/views/BonSortiesPage.vue`
- `frontend/src/views/FournisseursPage.vue`
- `frontend/src/views/MouvementsStockPage.vue`

**Problème :**  
`new Date(null).toLocaleString()` retournait silencieusement `"01/01/1970"` au lieu d'indiquer qu'il n'y a pas de date.

**Avant :**
```js
formaterDate(date) {
  return new Date(date).toLocaleString('fr-FR', { ... })
}
```

**Après :**
```js
formaterDate(date) {
  if (!date) return '—'
  const d = new Date(date)
  return isNaN(d) ? '—' : d.toLocaleString('fr-FR', { ... })
}
```

---

## 7. Contrôle des rôles dans l'UI — Boutons conditionnés

**Fichiers :**
- `frontend/src/views/CategoriesPage.vue`
- `frontend/src/views/BonReceptionsPage.vue`
- `frontend/src/views/BonSortiesPage.vue`

**Problème :**  
Les boutons Nouveau, Éditer, Valider, Désactiver et Supprimer étaient visibles pour tous les rôles (MAGASINIER, AUDITEUR inclus). Le backend bloquait bien les requêtes, mais l'UI laissait croire que l'action était possible — mauvaise expérience utilisateur.

**Correction :**
- Ajout des computed `peutEcrire` et `peutSupprimer` dans les 3 pages
- Les boutons sont maintenant conditionnés par `v-if="peutEcrire"` ou `v-if="peutSupprimer"`

**Tableau des droits UI après correction :**

| Page | Voir | Créer / Modifier | Valider / Supprimer |
|---|---|---|---|
| Tableau de bord | Tous | — | — |
| Entrepôts | Tous | ADMIN + GESTIONNAIRE | ADMIN |
| Zones | Tous | ADMIN + GESTIONNAIRE | ADMIN |
| Produits | Tous | ADMIN + GESTIONNAIRE | ADMIN |
| Catégories | Tous | ADMIN + GESTIONNAIRE | ADMIN + GESTIONNAIRE |
| Fournisseurs | Tous | ADMIN + GESTIONNAIRE | ADMIN + GESTIONNAIRE |
| Stocks | Tous | — | — |
| Mouvements | Tous | — | — |
| Bons réception | Tous | ADMIN + GESTIONNAIRE | ADMIN + GESTIONNAIRE |
| Bons sortie | Tous | ADMIN + GESTIONNAIRE | ADMIN + GESTIONNAIRE |
| Utilisateurs | ADMIN | ADMIN | ADMIN |

---

## Résumé des fichiers modifiés

### Backend
| Fichier | Type |
|---|---|
| `bootstrap/DataLoader.java` | Correction critique |
| `repository/UserRepository.java` | Supprimé |
| `entity/Stock.java` | Correction contrainte unique |
| `service/BonSortieService.java` | Optimisation + message d'erreur |

### Frontend
| Fichier | Type |
|---|---|
| `views/BonReceptionsPage.vue` | Rôles UI + alert() + formaterDate |
| `views/BonSortiesPage.vue` | Rôles UI + alert() + formaterDate |
| `views/CategoriesPage.vue` | Rôles UI |
| `views/FournisseursPage.vue` | formaterDate |
| `views/MouvementsStockPage.vue` | formaterDate |

---

## Message de commit Git

```
fix: corrections modules 1-9 — rôles, DataLoader, entités et UX

- DataLoader: créer catégories avant produits (FK Categorie au lieu de String)
- Supprimer UserRepository.java obsolète
- Stock: contrainte unique étendue à (produit_id, entrepot_id, zone_id)
- BonSortieService: appliquerSortie() itère directement sur SortieLigne (sans conversion DTO)
- BonReceptions/BonSorties: remplacer alert() par affichage dans form-error
- formaterDate: sécuriser contre les dates null/invalides (4 pages)
- Rôles UI: conditionner boutons selon rôle (CategoriesPage, BonReceptionsPage, BonSortiesPage)
```
