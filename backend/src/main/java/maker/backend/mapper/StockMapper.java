package maker.backend.mapper;

import maker.backend.dto.StockDTO;
import maker.backend.entity.Stock;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public StockDTO toDTO(Stock stock) {
        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setProduitId(stock.getProduit().getId());
        dto.setProduitReference(stock.getProduit().getReference());
        dto.setProduitNom(stock.getProduit().getNom());
        dto.setEntrepotId(stock.getEntrepot().getId());
        dto.setEntrepotNom(stock.getEntrepot().getNom());
        if (stock.getZone() != null) {
            dto.setZoneId(stock.getZone().getId());
            dto.setZoneNom(stock.getZone().getNom());
        }
        if (stock.getEmplacement() != null) {
            dto.setEmplacementId(stock.getEmplacement().getId());
            dto.setEmplacementCode(stock.getEmplacement().getCode());
            dto.setEmplacementCodeComplet(maker.backend.service.EmplacementService.buildCodeComplet(stock.getEmplacement()));
        }
        dto.setQuantiteDisponible(stock.getQuantiteDisponible());
        dto.setQuantiteReservee(stock.getQuantiteReservee());
        dto.setQuantiteTransit(stock.getQuantiteTransit());
        dto.setStockMin(stock.getStockMin());
        dto.setStockMax(stock.getStockMax());
        return dto;
    }
}
