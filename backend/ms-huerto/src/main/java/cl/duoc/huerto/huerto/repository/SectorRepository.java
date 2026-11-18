package cl.duoc.huerto.huerto.repository;

import cl.duoc.huerto.huerto.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository ya trae findAll, findById, save, deleteById, existsById y count.
public interface SectorRepository extends JpaRepository<Sector, Long> {

    // SELECT * FROM sectores ORDER BY nombre
    List<Sector> findAllByOrderByNombreAsc();
}
