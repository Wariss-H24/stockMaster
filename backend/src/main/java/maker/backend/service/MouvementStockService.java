package maker.backend.service;

import maker.backend.dto.MouvementStockDTO;
import maker.backend.entity.MouvementStock;
import maker.backend.mapper.MouvementStockMapper;
import maker.backend.repository.MouvementStockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MouvementStockService {

    private final MouvementStockRepository repo;
    private final MouvementStockMapper mapper;

    public MouvementStockService(MouvementStockRepository repo, MouvementStockMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<MouvementStockDTO> findAll() {
        return repo.findAll().stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public List<MouvementStockDTO> findByStockId(Long stockId) {
        return repo.findByStockIdOrderByDateDesc(stockId).stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    public List<MouvementStockDTO> findByType(String type) {
        try {
            return repo.findByTypeOrderByDateDesc(MouvementStock.Type.valueOf(type.toUpperCase()))
                    .stream().map(mapper::toDTO).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public MouvementStock enregistrer(MouvementStock mouvement) {
        return repo.save(mouvement);
    }
}
