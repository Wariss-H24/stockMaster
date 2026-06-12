package maker.backend.service;

import maker.backend.entity.ZoneFr;
import maker.backend.repository.ZoneFrRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneFrService {
    private final ZoneFrRepository repo;

    public ZoneFrService(ZoneFrRepository repo) { this.repo = repo; }

    public List<ZoneFr> findAll() { return repo.findAll(); }
    public ZoneFr save(ZoneFr z) { return repo.save(z); }
    public java.util.Optional<ZoneFr> findById(Long id) { return repo.findById(id); }
    public void delete(Long id) { repo.deleteById(id); }
}
