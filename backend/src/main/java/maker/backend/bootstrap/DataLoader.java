package maker.backend.bootstrap;

import maker.backend.entity.*;
import maker.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Chargement des données initiales au démarrage (seulement si les tables sont vides).
 */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(
            RoleRepository roleRepo,
            UtilisateurRepository userRepo,
            EntrepotRepository entrepotRepo,
            ZoneFrRepository zoneRepo,
            CategorieRepository categorieRepo,
            ProduitRepository produitRepo,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // --- Rôles ---
            if (roleRepo.count() == 0) {
                for (String nom : new String[]{"ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR"}) {
                    Role r = new Role();
                    r.setNom(nom);
                    roleRepo.save(r);
                }
            }

            // --- Utilisateurs de démonstration (mots de passe hashés BCrypt) ---
            if (userRepo.count() == 0) {
                Object[][] users = {
                    {"admin",        "admin123",  "Administrateur", "ADMIN"},
                    {"gestionnaire", "gest123",   "Pierre Martin",  "GESTIONNAIRE"},
                    {"magasinier",   "mag123",    "Sophie Durand",  "MAGASINIER"},
                    {"auditeur",     "audit123",  "Marc Leclerc",   "AUDITEUR"},
                };
                for (Object[] data : users) {
                    Utilisateur u = new Utilisateur();
                    u.setUsername((String) data[0]);
                    u.setMotDePasse(passwordEncoder.encode((String) data[1]));
                    u.setNomComplet((String) data[2]);
                    u.setActif(true);
                    roleRepo.findByNom((String) data[3]).ifPresent(r -> u.getRoles().add(r));
                    userRepo.save(u);
                }
            }

            // --- Entrepôts de démonstration ---
            if (entrepotRepo.count() == 0) {
                Entrepot e1 = new Entrepot();
                e1.setNom("ENT-Paris");
                e1.setAdresse("12 Rue de la Logistique, Paris");
                e1.setResponsable("Jean Dupont");
                e1.setCapaciteTotale(10000);
                e1.setCapaciteUtilisee(3200);
                e1.setActif(true);
                entrepotRepo.save(e1);

                Entrepot e2 = new Entrepot();
                e2.setNom("ENT-Lyon");
                e2.setAdresse("5 Avenue du Stock, Lyon");
                e2.setResponsable("Marie Martin");
                e2.setCapaciteTotale(8000);
                e2.setCapaciteUtilisee(5500);
                e2.setActif(true);
                entrepotRepo.save(e2);

                // Zones rattachées à ENT-Paris
                String[] nomsZones  = {"Zone A - Réception", "Zone B - Stockage", "Zone C - Expédition"};
                int[]    capTotales = {3000, 5000, 2000};
                int[]    capUtil    = {800,  2400, 0};
                for (int i = 0; i < nomsZones.length; i++) {
                    ZoneFr z = new ZoneFr();
                    z.setNom(nomsZones[i]);
                    z.setEntrepot(e1);
                    z.setCapaciteTotale(capTotales[i]);
                    z.setCapaciteUtilisee(capUtil[i]);
                    z.setActif(true);
                    zoneRepo.save(z);
                }
            }

            // --- Catégories de démonstration ---
            if (categorieRepo.count() == 0) {
                for (String[] data : new String[][]{
                        {"Informatique",   "Matériel et accessoires informatiques"},
                        {"Électroménager", "Appareils électroménagers"},
                        {"Pièces auto",    "Pièces et accessoires automobiles"},
                        {"Alimentaire",    "Produits alimentaires"},
                }) {
                    Categorie c = new Categorie();
                    c.setNom(data[0]);
                    c.setDescription(data[1]);
                    c.setActif(true);
                    categorieRepo.save(c);
                }
            }

            // --- Produits de démonstration ---
            if (produitRepo.count() == 0) {
                Categorie catInfo    = categorieRepo.findAll().stream().filter(c -> c.getNom().equals("Informatique")).findFirst().orElse(null);
                Categorie catElec    = categorieRepo.findAll().stream().filter(c -> c.getNom().equals("Électroménager")).findFirst().orElse(null);
                Categorie catAuto    = categorieRepo.findAll().stream().filter(c -> c.getNom().equals("Pièces auto")).findFirst().orElse(null);

                Object[][] produits = {
                    {"PRD-001", "1234567890123", "Laptop Dell XPS",       catInfo,  800.0, 1200.0, 2.1,  0.003},
                    {"PRD-002", "9876543210987", "Clavier mécanique",     catInfo,   60.0,   95.0, 0.9,  0.001},
                    {"PRD-003", "1111222233334", "Réfrigérateur Samsung", catElec,  350.0,  550.0, 45.0, 0.5  },
                    {"PRD-004", "5555666677778", "Filtre à huile VW",     catAuto,    8.0,   18.0, 0.3,  0.0002},
                };
                for (Object[] d : produits) {
                    Produit p = new Produit();
                    p.setReference((String) d[0]);
                    p.setCodeBarre((String) d[1]);
                    p.setNom((String) d[2]);
                    p.setCategorie((Categorie) d[3]);
                    p.setPrixAchat((Double) d[4]);
                    p.setPrixVente((Double) d[5]);
                    p.setPoids((Double) d[6]);
                    p.setVolume((Double) d[7]);
                    p.setSupprime(false);
                    produitRepo.save(p);
                }
            }
        };
    }
}
