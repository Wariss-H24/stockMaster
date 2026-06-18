package maker.backend.service;

import maker.backend.dto.EntrepotDTO;
import maker.backend.entity.Entrepot;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.mapper.EntrepotMapper;
import maker.backend.repository.EntrepotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EntrepotService {

    private final EntrepotRepository repo;
    private final EntrepotMapper mapper;

    public EntrepotService(EntrepotRepository repo, EntrepotMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<EntrepotDTO> findAll() {
        return repo.findByActifTrue().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public EntrepotDTO findById(Long id) {
        return repo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
    }

    public EntrepotDTO creer(EntrepotDTO dto) {
        Entrepot e = mapper.toEntity(dto);
        e.setActif(true);
        return mapper.toDTO(repo.save(e));
    }

    public EntrepotDTO modifier(Long id, EntrepotDTO dto) {
        Entrepot existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
        existing.setNom(dto.getNom());
        existing.setAdresse(dto.getAdresse());
        existing.setResponsable(dto.getResponsable());
        existing.setCapaciteTotale(dto.getCapaciteTotale());
        existing.setActif(dto.isActif());
        return mapper.toDTO(repo.save(existing));
    }

    // Désactivation logique
    public void desactiver(Long id) {
        Entrepot e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + id));
        e.setActif(false);
        repo.save(e);
    }
}
