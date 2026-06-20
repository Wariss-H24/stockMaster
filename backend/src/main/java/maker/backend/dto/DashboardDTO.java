package maker.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardDTO {
    // KPIs principaux
    private long nbEntrepots;
    private long nbProduits;
    private long nbStocksCritiques;
    private long nbStocksOk;
    private long entreesDuMois;
    private long sortiesDuMois;
    private long nbTransfertsEnCours;
    private long nbCommandesEnAttente;

    // Valeur totale du stock
    private double valeurTotaleStock;

    // Top 5 produits les plus mouvementés (nom → nb mouvements)
    private List<Map<String, Object>> topProduits;

    // Évolution 6 derniers mois (label mois → nb entrées, nb sorties)
    private List<Map<String, Object>> evolutionMensuelle;

    // Répartition par entrepôt (nom → quantité totale)
    private List<Map<String, Object>> repartitionEntrepots;

    // Alertes actives
    private List<AlerteDTO> alertesActives;
}
