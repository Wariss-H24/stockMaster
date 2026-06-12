# StockMaster - Backend

Ce document résume le travail réalisé jusqu'à présent dans le dossier `backend` du projet StockMaster. Il est rédigé en français (nomenclature des entités et endpoints en français).

## Objectif
Mise en place d'une base pour l'application de gestion de stocks, couvrant les modules 1 à 4 :

- Module 1 : gestion des utilisateurs (Authentification JWT prévue plus tard)
- Module 2 : gestion des entrepôts
- Module 3 : gestion des zones de stockage
- Module 4 : gestion des produits

Travail effectué : création des entités JPA, repositories, services, controllers REST, configuration H2 pour le développement et bootstrap de données d'exemple.

## Points clefs implémentés

- Nomenclature française : entités et endpoints utilisent le français (ex : `Entrepot`, `Produit`, `Utilisateur`).
- Structure en couches : `entity`, `repository`, `service`, `controller`.
- Base de données en mémoire (H2) configurée pour le dev ; `spring.jpa.hibernate.ddl-auto=update` dans `application.properties`.
- Sécurité Spring basique en mode développement : toutes les requêtes sont autorisées (fichier `SecurityConfig.java`).
- `DataLoader` : insertion initiale de rôles, utilisateur `admin`, un entrepôt, une zone et un produit.

## Fichiers importants créés / modifiés

- `pom.xml` : ajout des dépendances H2 et `spring-boot-starter-validation`.
- `src/main/resources/application.properties` : configuration H2, JPA, console H2.
- `src/main/java/maker/backend/entity/` : entités françaises
  - `Utilisateur.java`, `Role.java`, `Entrepot.java`, `ZoneFr.java`, `Produit.java`
- `src/main/java/maker/backend/repository/` : repositories JPA
  - `UtilisateurRepository.java`, `RoleRepositoryFr.java`, `EntrepotRepository.java`, `ZoneFrRepository.java`, `ProduitRepository.java`
- `src/main/java/maker/backend/service/` : services
  - `UtilisateurService.java`, `EntrepotService.java`, `ZoneFrService.java`, `ProduitService.java`
- `src/main/java/maker/backend/controller/` : controllers REST (endpoints en français)
  - `UtilisateurController.java` -> `/api/utilisateurs`
  - `EntrepotController.java` -> `/api/entrepots`
  - `ZoneFrController.java` -> `/api/zones`
  - `ProduitController.java` -> `/api/produits`
- `src/main/java/maker/backend/bootstrap/DataLoader.java` : bootstrap de données d'exemple.
- `src/main/java/maker/backend/config/SecurityConfig.java` : configuration de sécurité permissive pour dev.

Quelques anciens fichiers en anglais ont été marqués `@Deprecated` (controllers/repositories) pour éviter les conflits mais sont laissés en place pour compatibilité temporaire.

## Endpoints REST (exemples)

- Utilisateurs
  - GET  /api/utilisateurs
  - POST /api/utilisateurs
  - GET  /api/utilisateurs/{id}
  - PUT  /api/utilisateurs/{id}
  - DELETE /api/utilisateurs/{id}

- Entrepôts
  - GET  /api/entrepots
  - POST /api/entrepots
  - GET  /api/entrepots/{id}
  - PUT  /api/entrepots/{id}
  - DELETE /api/entrepots/{id}

- Zones
  - GET  /api/zones
  - POST /api/zones
  - GET  /api/zones/{id}
  - PUT  /api/zones/{id}
  - DELETE /api/zones/{id}

- Produits
  - GET  /api/produits
  - POST /api/produits
  - GET  /api/produits/{id}
  - PUT  /api/produits/{id}
  - DELETE /api/produits/{id} (suppression logique)

Exemples `curl` (après démarrage) :

```bash
curl http://localhost:8080/api/produits
curl http://localhost:8080/api/utilisateurs
curl http://localhost:8080/api/entrepots
```

## Schéma de base de données (résumé)

Tables principales (noms recommandés, JPA génère automatiquement des tables similaires) :

- `role` (id, nom)
- `utilisateur` (id, username, mot_de_passe, nom_complet, actif)
- `utilisateur_roles` (utilisateur_id, role_id)
- `entrepot` (id, nom, adresse, responsable, capacite_totale, capacite_utilisee, actif)
- `zone` (id, nom, entrepot_id, capacite_totale, capacite_utilisee)
- `produit` (id, reference, code_barre, nom, categorie, description, prix_achat, prix_vente, poids, volume, supprime)

Pour un schéma DDL complet et la mise en place via Flyway, voir proposition de script SQL (je peux l'ajouter si tu veux).

## Comment builder et lancer (dev)

Depuis le répertoire `backend` :

```bash
# build
./mvnw -DskipTests package

# lancer
./mvnw spring-boot:run
```

Si le `mvnw` échoue à télécharger Maven, utiliser l'installation système :

```bash
mvn -DskipTests package
mvn spring-boot:run
```

H2 console : http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:stockdb`, user: `sa`, password: ` `)

## Remarques et prochaines étapes proposées

1. Valider/normaliser les noms des tables/colonnes via `@Table`/`@Column` sur les entités si besoin.
2. Ajouter Flyway (ou Liquibase) et déposer le DDL initial (`V1__init.sql`).
3. Remplacer la sécurité permissive par JWT (Module 1) : ajouter `AuthenticationController`, `JwtProvider`, `UserDetailsService`.
4. Ajouter DTOs + MapStruct pour séparer entités & API.
5. Construire le frontend Vue (en français) et configurer le proxy Vite vers `/api`.
6. Ajouter les modules suivants : catégories, fournisseurs, stocks, mouvements, transferts, inventaires.

Si tu veux, je peux :
- Générer le fichier Flyway `V1__init.sql` automatiquement et l'ajouter dans `src/main/resources/db/migration/`.
- Créer le frontend Vue en français (scaffold Vite + pages CRUD minimalistes pour utilisateurs/entrepots/zones/produits).

Indique ce que tu préfères que je fasse ensuite et je m'en occupe.

---

README généré automatiquement : décrit le travail effectué et comment démarrer l'environnement de développement.
