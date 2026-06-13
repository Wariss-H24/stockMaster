package maker.backend.mapper;

import maker.backend.dto.EntrepotDTO;
import maker.backend.entity.Entrepot;
import org.springframework.stereotype.Component;

/**
 * Mapper manuel Entrepot ↔ EntrepotDTO.
 */
@Component
public class EntrepotMapper {

    public EntrepotDTO toDTO(Entrepot e) {
        EntrepotDTO dto = new EntrepotDTO();
        dto.setId(e.getId());
        dto.setNom(e.getNom());
        dto.setAdresse(e.getAdresse());
        dto.setResponsable(e.getResponsable());
        dto.setCapaciteTotale(e.getCapaciteTotale());
        dto.setCapaciteUtilisee(e.getCapaciteUtilisee());
        dto.setActif(e.isActif());
        // Calcul du taux d'occupation
        if (e.getCapaciteTotale() != null && e.getCapaciteTotale() > 0) {
            dto.setTauxOccupation((int) Math.round((e.getCapaciteUtilisee() * 100.0) / e.getCapaciteTotale()));
        } else {
            dto.setTauxOccupation(0);
        }
        return dto;
    }

    public Entrepot toEntity(EntrepotDTO dto) {
        Entrepot e = new Entrepot();
        e.setNom(dto.getNom());
        e.setAdresse(dto.getAdresse());
        e.setResponsable(dto.getResponsable());
        e.setCapaciteTotale(dto.getCapaciteTotale());
        e.setCapaciteUtilisee(dto.getCapaciteUtilisee() != null ? dto.getCapaciteUtilisee() : 0);
        e.setActif(dto.isActif());
        return e;
    }
}
