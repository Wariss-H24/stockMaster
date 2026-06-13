package maker.backend.bootstrap;

import maker.backend.entity.*;
import maker.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            ProduitRepository produitRepo) {

        return args -> {

            // --- Rôles ---
            if (roleRepo.count() == 0) {
                for (String nom : new String[]{"ADMIN", "GESTIONNAIRE", "MAGASINIER", "AUDITEUR"}) {
                    Role r = new Role();
                    r.setNom(nom);
                    roleRepo.save(r);
                }
            }

            // --- Utilisateur admin par défaut ---
            if (userRepo.count() == 0) {
                Utilisateur admin = new Utilisateur();
                admin.setUsername("admin");
                admin.setMotDePasse("admin123"); // À hasher avec BCrypt au module JWT
                admin.setNomComplet("Administrateur");
                admin.setActif(true);
                roleRepo.findByNom("ADMIN").ifPresent(r -> admin.getRoles().add(r));
                userRepo.save(admin);
            }

            // --- Entrepôt de démonstration ---
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

                // --- Zones ---
                String[] nomsZones = {"Zone A - Réception", "Zone B - Stockage", "Zone C - Expédition"};
                int[] capTotales = {3000, 5000, 2000};
                int[] capUtilisees = {800, 2400, 0};
                for (int i = 0; i < nomsZones.length; i++) {
                    ZoneFr z = new ZoneFr();
                    z.setNom(nomsZones[i]);
                    z.setEntrepot(e1);
                    z.setCapaciteTotale(capTotales[i]);
                    z.setCapaciteUtilisee(capUtilisees[i]);
                    z.setActif(true);
                    zoneRepo.save(z);
                }
            }

            // --- Produits de démonstration ---
            if (produitRepo.count() == 0) {
                Object[][] produits = {
                    {"PRD-001", "1234567890123", "Laptop Dell XPS", "Informatique", 800.0, 1200.0, 2.1, 0.003},
                    {"PRD-002", "9876543210987", "Clavier mécanique", "Informatique", 60.0, 95.0, 0.9, 0.001},
                    {"PRD-003", "1111222233334", "Réfrigérateur Samsung", "Électroménager", 350.0, 550.0, 45.0, 0.5},
                    {"PRD-004", "5555666677778", "Filtre à huile VW", "Pièces auto", 8.0, 18.0, 0.3, 0.0002},
                };
                for (Object[] data : produits) {
                    Produit p = new Produit();
                    p.setReference((String) data[0]);
                    p.setCodeBarre((String) data[1]);
                    p.setNom((String) data[2]);
                    p.setCategorie((String) data[3]);
                    p.setPrixAchat((Double) data[4]);
                    p.setPrixVente((Double) data[5]);
                    p.setPoids((Double) data[6]);
                    p.setVolume((Double) data[7]);
                    p.setSupprime(false);
                    produitRepo.save(p);
                }
            }
        };
    }
}
