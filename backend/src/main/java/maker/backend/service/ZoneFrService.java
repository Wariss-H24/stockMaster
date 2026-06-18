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

        verifierCapaciteEntrepot(entrepot, null, dto.getCapaciteTotale());

        ZoneFr z = mapper.toEntity(dto, entrepot);
        return mapper.toDTO(repo.save(z));
    }

    public ZoneFrDTO modifier(Long id, ZoneFrDTO dto) {
        ZoneFr existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
        Entrepot entrepot = entrepotRepo.findById(dto.getEntrepotId())
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt introuvable : " + dto.getEntrepotId()));

        verifierCapaciteEntrepot(entrepot, id, dto.getCapaciteTotale());

        existing.setNom(dto.getNom());
        existing.setEntrepot(entrepot);
        existing.setCapaciteTotale(dto.getCapaciteTotale());
        existing.setActif(dto.isActif());
        return mapper.toDTO(repo.save(existing));
    }

    public void desactiver(Long id) {
        ZoneFr z = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + id));
        z.setActif(false);
        repo.save(z);
    }

    /**
     * Vérifie que la somme des capacités totales des zones actives de l'entrepôt
     * (en excluant éventuellement la zone en cours de modification) ne dépasse pas
     * la capacité totale de l'entrepôt.
     *
     * @param entrepot       l'entrepôt cible
     * @param zoneIdExclure  ID de la zone à exclure du calcul (modification), null pour une création
     * @param nouvelleCapacite capacité totale de la zone à créer/modifier
     */
    private void verifierCapaciteEntrepot(Entrepot entrepot, Long zoneIdExclure, Integer nouvelleCapacite) {
        if (entrepot.getCapaciteTotale() == null || entrepot.getCapaciteTotale() == 0) return;
        if (nouvelleCapacite == null || nouvelleCapacite == 0) return;

        int sommeExistantes = repo.findByEntrepotIdAndActifTrue(entrepot.getId()).stream()
                .filter(z -> !z.getId().equals(zoneIdExclure))
                .mapToInt(z -> z.getCapaciteTotale() != null ? z.getCapaciteTotale() : 0)
                .sum();

        int total = sommeExistantes + nouvelleCapacite;
        if (total > entrepot.getCapaciteTotale()) {
            throw new IllegalArgumentException(
                "Capacité dépassée : la somme des capacités des zones (" + total +
                ") dépasse la capacité totale de l'entrepôt « " + entrepot.getNom() +
                " » (" + entrepot.getCapaciteTotale() + ")."
            );
        }
    }
}
