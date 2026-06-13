package maker.backend.mapper;

import maker.backend.dto.ZoneFrDTO;
import maker.backend.entity.Entrepot;
import maker.backend.entity.ZoneFr;
import org.springframework.stereotype.Component;

/**
 * Mapper manuel ZoneFr ↔ ZoneFrDTO.
 */
@Component
public class ZoneFrMapper {

    public ZoneFrDTO toDTO(ZoneFr z) {
        ZoneFrDTO dto = new ZoneFrDTO();
        dto.setId(z.getId());
        dto.setNom(z.getNom());
        dto.setCapaciteTotale(z.getCapaciteTotale());
        dto.setCapaciteUtilisee(z.getCapaciteUtilisee());
        dto.setActif(z.isActif());
        // Inclure l'ID et le nom de l'entrepôt parent
        if (z.getEntrepot() != null) {
            dto.setEntrepotId(z.getEntrepot().getId());
            dto.setEntrepotNom(z.getEntrepot().getNom());
        }
        // Calcul du taux d'occupation
        if (z.getCapaciteTotale() != null && z.getCapaciteTotale() > 0) {
            dto.setTauxOccupation((int) Math.round((z.getCapaciteUtilisee() * 100.0) / z.getCapaciteTotale()));
        } else {
            dto.setTauxOccupation(0);
        }
        return dto;
    }

    public ZoneFr toEntity(ZoneFrDTO dto, Entrepot entrepot) {
        ZoneFr z = new ZoneFr();
        z.setNom(dto.getNom());
        z.setEntrepot(entrepot);
        z.setCapaciteTotale(dto.getCapaciteTotale());
        z.setCapaciteUtilisee(dto.getCapaciteUtilisee() != null ? dto.getCapaciteUtilisee() : 0);
        z.setActif(true);
        return z;
    }
}
