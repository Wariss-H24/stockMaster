package maker.backend.mapper;

import maker.backend.dto.CategorieDTO;
import maker.backend.entity.Categorie;
import org.springframework.stereotype.Component;

@Component
public class CategorieMapper {

    public CategorieDTO toDTO(Categorie c) {
        CategorieDTO dto = new CategorieDTO();
        dto.setId(c.getId());
        dto.setNom(c.getNom());
        dto.setDescription(c.getDescription());
        dto.setActif(c.isActif());
        return dto;
    }

    public Categorie toEntity(CategorieDTO dto) {
        Categorie c = new Categorie();
        c.setNom(dto.getNom());
        c.setDescription(dto.getDescription());
        c.setActif(dto.isActif());
        return c;
    }
}
