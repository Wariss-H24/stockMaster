package maker.backend.service;

import maker.backend.entity.Utilisateur;
import maker.backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurService {

    private final UtilisateurRepository repo;

    public UtilisateurService(UtilisateurRepository repo) { this.repo = repo; }

    public List<Utilisateur> findAll() { return repo.findAll(); }

    public Utilisateur save(Utilisateur u) { return repo.save(u); }

    public java.util.Optional<Utilisateur> findById(Long id) { return repo.findById(id); }

    public void delete(Long id) { repo.deleteById(id); }
}
