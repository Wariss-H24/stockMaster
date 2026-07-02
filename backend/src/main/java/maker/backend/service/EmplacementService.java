package maker.backend.service;

import maker.backend.dto.EtagereDTO;
import maker.backend.dto.EmplacementDTO;
import maker.backend.dto.RayonDTO;
import maker.backend.entity.*;
import maker.backend.exception.ResourceNotFoundException;
import maker.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 17 — Gestion de la hiérarchie Rayon → Étagère → Emplacement.
 */
@Service
@Transactional
public class EmplacementService {

    private final RayonRepository rayonRepo;
    private final EtagereRepository etagereRepo;
    private final EmplacementRepository emplacementRepo;
    private final ZoneFrRepository zoneRepo;

    public EmplacementService(RayonRepository rayonRepo,
                               EtagereRepository etagereRepo,
                               EmplacementRepository emplacementRepo,
                               ZoneFrRepository zoneRepo) {
        this.rayonRepo      = rayonRepo;
        this.etagereRepo    = etagereRepo;
        this.emplacementRepo = emplacementRepo;
        this.zoneRepo       = zoneRepo;
    }

    // ── RAYONS ────────────────────────────────────────────────────────────────

    public List<RayonDTO> findAllRayons() {
        return rayonRepo.findAll().stream().map(this::toRayonDTO).collect(Collectors.toList());
    }

    public List<RayonDTO> findRayonsByZone(Long zoneId) {
        return rayonRepo.findByZoneIdOrderByNomAsc(zoneId)
                .stream().map(this::toRayonDTO).collect(Collectors.toList());
    }

    public RayonDTO findRayonById(Long id) {
        return toRayonDTO(getRayonOrThrow(id));
    }

    public RayonDTO creerRayon(RayonDTO dto) {
        ZoneFr zone = zoneRepo.findById(dto.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone introuvable : " + dto.getZoneId()));

        if (dto.getCode() != null && rayonRepo.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Un rayon avec le code « " + dto.getCode() + " » existe déjà.");
        }

        Rayon r = new Rayon();
        r.setNom(dto.getNom());
        r.setCode(dto.getCode() != null ? dto.getCode() : genererCodeRayon(zone));
        r.setZone(zone);
        r.setDescription(dto.getDescription());
        r.setActif(true);
        return toRayonDTO(rayonRepo.save(r));
    }

    public RayonDTO modifierRayon(Long id, RayonDTO dto) {
        Rayon r = getRayonOrThrow(id);
        r.setNom(dto.getNom());
        r.setDescription(dto.getDescription());
        return toRayonDTO(rayonRepo.save(r));
    }

    public void desactiverRayon(Long id) {
        Rayon r = getRayonOrThrow(id);
        r.setActif(false);
        rayonRepo.save(r);
    }

    // ── ÉTAGÈRES ──────────────────────────────────────────────────────────────

    public List<EtagereDTO> findAllEtageres() {
        return etagereRepo.findAll().stream().map(this::toEtagereDTO).collect(Collectors.toList());
    }

    public List<EtagereDTO> findEtageresByRayon(Long rayonId) {
        return etagereRepo.findByRayonIdOrderByNomAsc(rayonId)
                .stream().map(this::toEtagereDTO).collect(Collectors.toList());
    }

    public EtagereDTO findEtagereById(Long id) {
        return toEtagereDTO(getEtagereOrThrow(id));
    }

    public EtagereDTO creerEtagere(EtagereDTO dto) {
        Rayon rayon = getRayonOrThrow(dto.getRayonId());

        if (dto.getCode() != null && etagereRepo.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Une étagère avec le code « " + dto.getCode() + " » existe déjà.");
        }

        Etagere e = new Etagere();
        e.setNom(dto.getNom());
        e.setCode(dto.getCode() != null ? dto.getCode() : genererCodeEtagere(rayon));
        e.setRayon(rayon);
        e.setNiveaux(dto.getNiveaux());
        e.setActif(true);
        return toEtagereDTO(etagereRepo.save(e));
    }

    public EtagereDTO modifierEtagere(Long id, EtagereDTO dto) {
        Etagere e = getEtagereOrThrow(id);
        e.setNom(dto.getNom());
        e.setNiveaux(dto.getNiveaux());
        return toEtagereDTO(etagereRepo.save(e));
    }

    public void desactiverEtagere(Long id) {
        Etagere e = getEtagereOrThrow(id);
        e.setActif(false);
        etagereRepo.save(e);
    }

    // ── EMPLACEMENTS ──────────────────────────────────────────────────────────

    public List<EmplacementDTO> findAllEmplacements() {
        return emplacementRepo.findAll().stream().map(this::toEmplacementDTO).collect(Collectors.toList());
    }

    public List<EmplacementDTO> findEmplacementsByEtagere(Long etagereId) {
        return emplacementRepo.findByEtagereIdOrderByNomAsc(etagereId)
                .stream().map(this::toEmplacementDTO).collect(Collectors.toList());
    }

    public List<EmplacementDTO> findEmplacementsByEntrepot(Long entrepotId) {
        return emplacementRepo.findByEntrepotId(entrepotId)
                .stream().map(this::toEmplacementDTO).collect(Collectors.toList());
    }

    public List<EmplacementDTO> findEmplacementsByZone(Long zoneId) {
        return emplacementRepo.findByZoneId(zoneId)
                .stream().map(this::toEmplacementDTO).collect(Collectors.toList());
    }

    public EmplacementDTO findEmplacementById(Long id) {
        return toEmplacementDTO(getEmplacementOrThrow(id));
    }

    public EmplacementDTO creerEmplacement(EmplacementDTO dto) {
        Etagere etagere = getEtagereOrThrow(dto.getEtagereId());

        if (emplacementRepo.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Un emplacement avec le code « " + dto.getCode() + " » existe déjà.");
        }

        Emplacement emp = new Emplacement();
        emp.setNom(dto.getNom());
        emp.setCode(dto.getCode());
        emp.setEtagere(etagere);
        emp.setCapaciteMax(dto.getCapaciteMax());
        emp.setCapaciteUtilisee(0);
        emp.setActif(true);
        return toEmplacementDTO(emplacementRepo.save(emp));
    }

    public EmplacementDTO modifierEmplacement(Long id, EmplacementDTO dto) {
        Emplacement emp = getEmplacementOrThrow(id);
        emp.setNom(dto.getNom());
        emp.setCapaciteMax(dto.getCapaciteMax());
        return toEmplacementDTO(emplacementRepo.save(emp));
    }

    public void desactiverEmplacement(Long id) {
        Emplacement emp = getEmplacementOrThrow(id);
        emp.setActif(false);
        emplacementRepo.save(emp);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Rayon getRayonOrThrow(Long id) {
        return rayonRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rayon introuvable : " + id));
    }

    private Etagere getEtagereOrThrow(Long id) {
        return etagereRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Étagère introuvable : " + id));
    }

    private Emplacement getEmplacementOrThrow(Long id) {
        return emplacementRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable : " + id));
    }

    private String genererCodeRayon(ZoneFr zone) {
        long count = rayonRepo.findByZoneOrderByNomAsc(zone).size() + 1;
        return "RAYON-" + String.format("%02d", count);
    }

    private String genererCodeEtagere(Rayon rayon) {
        long count = etagereRepo.findByRayonOrderByNomAsc(rayon).size() + 1;
        return "ETAGERE-" + String.format("%02d", count);
    }

    /** Construit le code complet d'un emplacement. */
    public static String buildCodeComplet(Emplacement emp) {
        Etagere et = emp.getEtagere();
        Rayon r    = et.getRayon();
        ZoneFr z   = r.getZone();
        return z.getEntrepot().getNom() + "/" + z.getNom() + "/" + r.getCode() + "/" + et.getCode() + "/" + emp.getCode();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private RayonDTO toRayonDTO(Rayon r) {
        RayonDTO dto = new RayonDTO();
        dto.setId(r.getId());
        dto.setNom(r.getNom());
        dto.setCode(r.getCode());
        dto.setZoneId(r.getZone().getId());
        dto.setZoneNom(r.getZone().getNom());
        dto.setEntrepotId(r.getZone().getEntrepot().getId());
        dto.setEntrepotNom(r.getZone().getEntrepot().getNom());
        dto.setDescription(r.getDescription());
        dto.setActif(r.isActif());
        dto.setNbEtageres((int) etagereRepo.findByRayonOrderByNomAsc(r).size());
        return dto;
    }

    private EtagereDTO toEtagereDTO(Etagere e) {
        EtagereDTO dto = new EtagereDTO();
        dto.setId(e.getId());
        dto.setNom(e.getNom());
        dto.setCode(e.getCode());
        dto.setRayonId(e.getRayon().getId());
        dto.setRayonNom(e.getRayon().getNom());
        dto.setZoneId(e.getRayon().getZone().getId());
        dto.setZoneNom(e.getRayon().getZone().getNom());
        dto.setEntrepotId(e.getRayon().getZone().getEntrepot().getId());
        dto.setEntrepotNom(e.getRayon().getZone().getEntrepot().getNom());
        dto.setNiveaux(e.getNiveaux());
        dto.setActif(e.isActif());
        dto.setNbEmplacements((int) emplacementRepo.findByEtagereOrderByNomAsc(e).size());
        return dto;
    }

    private EmplacementDTO toEmplacementDTO(Emplacement emp) {
        EmplacementDTO dto = new EmplacementDTO();
        dto.setId(emp.getId());
        dto.setNom(emp.getNom());
        dto.setCode(emp.getCode());
        dto.setEtagereId(emp.getEtagere().getId());
        dto.setEtagereNom(emp.getEtagere().getNom());
        dto.setRayonId(emp.getEtagere().getRayon().getId());
        dto.setRayonNom(emp.getEtagere().getRayon().getNom());
        dto.setZoneId(emp.getEtagere().getRayon().getZone().getId());
        dto.setZoneNom(emp.getEtagere().getRayon().getZone().getNom());
        dto.setEntrepotId(emp.getEtagere().getRayon().getZone().getEntrepot().getId());
        dto.setEntrepotNom(emp.getEtagere().getRayon().getZone().getEntrepot().getNom());
        dto.setCodeComplet(buildCodeComplet(emp));
        dto.setCapaciteMax(emp.getCapaciteMax());
        dto.setCapaciteUtilisee(emp.getCapaciteUtilisee() != null ? emp.getCapaciteUtilisee() : 0);
        dto.setActif(emp.isActif());
        if (emp.getCapaciteMax() != null && emp.getCapaciteMax() > 0 && emp.getCapaciteUtilisee() != null) {
            dto.setTauxOccupation((int) Math.round(emp.getCapaciteUtilisee() * 100.0 / emp.getCapaciteMax()));
        }
        return dto;
    }
}
