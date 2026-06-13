package maker.backend.service;

import maker.backend.dto.ZoneFrDTO;
import maker.backend.entity.Entrepot;
import maker.backend.entity.ZoneFr;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.ZoneFrMapper;
import maker.backend.repository.EntrepotRepository;
import maker.backend.repository.ZoneFrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ZoneFrService {

    private final ZoneFrRepository repo;
    private final EntrepotRepository entrepotRepo;
    private final ZoneFrMapper mapper;

    public ZoneFrService(ZoneFrRepository repo, EntrepotRepository entrepotRepo, ZoneFrMapper mapper) {
        this.repo = repo;
        this.entrepotRepo = entrepotRepo;
        this.mapper = mapper;
    }

    public List<ZoneFrDTO> findAll() {
        return repo.findByActifTrue().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public ZoneFrDTO findById(Long id) {
        return repo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
    }

    public ZoneFrDTO creer(ZoneFrDTO dto) {
        Entrepot entrepot = entrepotRepo.findById(dto.getEntrepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + dto.getEntrepotId()));
        ZoneFr z = mapper.toEntity(dto, entrepot);
        return mapper.toDTO(repo.save(z));
    }

    public ZoneFrDTO modifier(Long id, ZoneFrDTO dto) {
        ZoneFr existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
        Entrepot entrepot = entrepotRepo.findById(dto.getEntrepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + dto.getEntrepotId()));
        existing.setNom(dto.getNom());
        existing.setEntrepot(entrepot);
        existing.setCapaciteTotale(dto.getCapaciteTotale());
        existing.setCapaciteUtilisee(dto.getCapaciteUtilisee());
        existing.setActif(dto.isActif());
        return mapper.toDTO(repo.save(existing));
    }

    // Désactivation logique
    public void desactiver(Long id) {
        ZoneFr z = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
        z.setActif(false);
        repo.save(z);
    }
}
