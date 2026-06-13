package maker.backend.bootstrap;

import maker.backend.entity.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner init(maker.backend.repository.RoleRepositoryFr roleRepo, maker.backend.repository.UtilisateurRepository userRepo,
                          maker.backend.repository.EntrepotRepository warehouseRepo,
                          maker.backend.repository.ZoneFrRepository zoneRepo,
                          maker.backend.repository.ProduitRepository productRepo) {
        return args -> {
            if (roleRepo.count() == 0) {
                Role r1 = new Role(); r1.setNom("ADMIN"); roleRepo.save(r1);
                Role r2 = new Role(); r2.setNom("GESTIONNAIRE"); roleRepo.save(r2);
            }

            if (userRepo.count() == 0) {
                Utilisateur u = new Utilisateur();
                u.setUsername("admin");
                u.setMotDePasse("admin");
                u.setNomComplet("Administrateur");
                u.getRoles().add(roleRepo.findAll().get(0));
                userRepo.save(u);
            }

            if (warehouseRepo.count() == 0) {
                Entrepot e = new Entrepot();
                e.setNom("ENT-001");
                e.setAdresse("123 Rue du Stock");
                e.setResponsable("Jean Dupont");
                e.setCapaciteTotale(10000);
                e.setCapaciteUtilisee(100);
                warehouseRepo.save(e);

                ZoneFr z = new ZoneFr();
                z.setNom("ZONE-A");
                z.setEntrepot(e);
                z.setCapaciteTotale(5000);
                z.setCapaciteUtilisee(50);
                zoneRepo.save(z);
            }

            if (productRepo.count() == 0) {
                Produit p = new Produit();
                p.setReference("PRD-001");
                p.setCodeBarre("1234567890123");
                p.setNom("Produit exemple");
                p.setCategorie("Informatique");
                p.setDescription("Produit exemple pour démarrage");
                p.setPrixAchat(10.0);
                p.setPrixVente(15.0);
                p.setPoids(0.5);
                p.setVolume(0.001);
                productRepo.save(p);
            }
        };
    }
}
