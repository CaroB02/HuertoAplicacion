package cl.duoc.huerto.huerto.repository;

import cl.duoc.huerto.huerto.model.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    // SELECT * FROM cultivos ORDER BY nombre
    List<Cultivo> findAllByOrderByNombreAsc();

    // SELECT * FROM cultivos WHERE sector_id = ?
    List<Cultivo> findBySectorId(Long sectorId);

    // ¿El sector tiene cultivos? (no se puede borrar un sector con cultivos)
    boolean existsBySectorId(Long sectorId);
}
