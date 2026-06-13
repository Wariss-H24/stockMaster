package maker.backend.mapper;

import maker.backend.dto.ProduitDTO;
import maker.backend.entity.Produit;
import org.springframework.stereotype.Component;

/**
 * Mapper manuel Produit ↔ ProduitDTO.
 */
@Component
public class ProduitMapper {

    public ProduitDTO toDTO(Produit p) {
        ProduitDTO dto = new ProduitDTO();
        dto.setId(p.getId());
        dto.setReference(p.getReference());
        dto.setCodeBarre(p.getCodeBarre());
        dto.setNom(p.getNom());
        dto.setCategorie(p.getCategorie());
        dto.setDescription(p.getDescription());
        dto.setPrixAchat(p.getPrixAchat());
        dto.setPrixVente(p.getPrixVente());
        dto.setPoids(p.getPoids());
        dto.setVolume(p.getVolume());
        dto.setSupprime(p.isSupprime());
        return dto;
    }

    public Produit toEntity(ProduitDTO dto) {
        Produit p = new Produit();
        p.setReference(dto.getReference());
        p.setCodeBarre(dto.getCodeBarre());
        p.setNom(dto.getNom());
        p.setCategorie(dto.getCategorie());
        p.setDescription(dto.getDescription());
        p.setPrixAchat(dto.getPrixAchat());
        p.setPrixVente(dto.getPrixVente());
        p.setPoids(dto.getPoids());
        p.setVolume(dto.getVolume());
        p.setSupprime(false);
        return p;
    }
}
