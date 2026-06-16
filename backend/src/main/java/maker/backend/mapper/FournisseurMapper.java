package maker.backend.mapper;

import maker.backend.dto.FournisseurDTO;
import maker.backend.entity.Fournisseur;
import org.springframework.stereotype.Component;

@Component
public class FournisseurMapper {

    public FournisseurDTO toDTO(Fournisseur f) {
        FournisseurDTO dto = new FournisseurDTO();
        dto.setId(f.getId());
        dto.setNom(f.getNom());
        dto.setAdresse(f.getAdresse());
        dto.setTelephone(f.getTelephone());
        dto.setEmail(f.getEmail());
        dto.setContactPrincipal(f.getContactPrincipal());
        dto.setActif(f.isActif());
        return dto;
    }

    public Fournisseur toEntity(FournisseurDTO dto) {
        Fournisseur f = new Fournisseur();
        f.setNom(dto.getNom());
        f.setAdresse(dto.getAdresse());
        f.setTelephone(dto.getTelephone());
        f.setEmail(dto.getEmail());
        f.setContactPrincipal(dto.getContactPrincipal());
        f.setActif(dto.isActif());
        return f;
    }
}
