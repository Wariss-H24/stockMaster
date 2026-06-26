package maker.backend.service;

import maker.backend.dto.EmplacementDTO;
import maker.backend.entity.Emplacement;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.EmplacementRepository;
import maker.backend.repository.ZoneFrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmplacementService {

    private final EmplacementRepository repo;
    private final ZoneFrRepository zoneRepo;

    public EmplacementService(EmplacementRepository repo, ZoneFrRepository zoneRepo) {
        this.repo = repo;
        this.zoneRepo = zoneRepo;
    }

    public List<EmplacementDTO> findAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EmplacementDTO> findByZone(Long zoneId) {
        return repo.findByZoneId(zoneId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EmplacementDTO> findByEntrepot(Long entrepotId) {
        return repo.findByZoneEntrepotId(entrepotId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EmplacementDTO> findDisponibles() {
        return repo.findByOccupeFalse().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public EmplacementDTO findById(Long id) {
        return toDTO(getOrThrow(id));
    }

    public EmplacementDTO creer(EmplacementDTO dto) {
        if (repo.existsByZoneIdAndRayonAndEtagereAndCode(dto.getZoneId(), dto.getRayon(), dto.getEtagere(), dto.getCode())) {
            throw new IllegalArgumentException("Un emplacement avec ce code existe déjà dans cette zone/rayon/étagère.");
        }
        return toDTO(repo.save(toEntity(dto)));
    }

    public EmplacementDTO modifier(Long id, EmplacementDTO dto) {
        Emplacement e = getOrThrow(id);
        e.setZone(zoneRepo.findById(dto.getZoneId())
            .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + dto.getZoneId())));
        e.setRayon(dto.getRayon());
        e.setEtagere(dto.getEtagere());
        e.setCode(dto.getCode());
        e.setOccupe(dto.isOccupe());
        return toDTO(repo.save(e));
    }

    public void supprimer(Long id) {
        Emplacement e = getOrThrow(id);
        if (e.isOccupe()) throw new IllegalStateException("Impossible de supprimer un emplacement occupé.");
        repo.delete(e);
    }

    private Emplacement getOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable : " + id));
    }

    private Emplacement toEntity(EmplacementDTO dto) {
        Emplacement e = new Emplacement();
        e.setZone(zoneRepo.findById(dto.getZoneId())
            .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + dto.getZoneId())));
        e.setRayon(dto.getRayon());
        e.setEtagere(dto.getEtagere());
        e.setCode(dto.getCode());
        e.setOccupe(dto.isOccupe());
        return e;
    }

    public EmplacementDTO toDTO(Emplacement e) {
        EmplacementDTO dto = new EmplacementDTO();
        dto.setId(e.getId());
        dto.setZoneId(e.getZone().getId());
        dto.setZoneNom(e.getZone().getNom());
        dto.setEntrepotNom(e.getZone().getEntrepot().getNom());
        dto.setRayon(e.getRayon());
        dto.setEtagere(e.getEtagere());
        dto.setCode(e.getCode());
        dto.setOccupe(e.isOccupe());
        dto.setCodeComplet(e.getCodeComplet());
        return dto;
    }
}
