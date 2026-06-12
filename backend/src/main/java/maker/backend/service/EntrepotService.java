package maker.backend.service;

import maker.backend.entity.Entrepot;
import maker.backend.repository.EntrepotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntrepotService {
    private final EntrepotRepository repo;

    public EntrepotService(EntrepotRepository repo) { this.repo = repo; }

    public List<Entrepot> findAll() { return repo.findAll(); }
    public Entrepot save(Entrepot e) { return repo.save(e); }
    public java.util.Optional<Entrepot> findById(Long id) { return repo.findById(id); }
    public void delete(Long id) { repo.deleteById(id); }
}
