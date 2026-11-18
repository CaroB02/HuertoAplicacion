package cl.duoc.huerto.huerto.repository;

import cl.duoc.huerto.huerto.model.LecturaSensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LecturaSensorRepository extends JpaRepository<LecturaSensor, Long> {

    // Todas las lecturas, la más reciente primero
    List<LecturaSensor> findAllByOrderByFechaDesc();

    // Al borrar un sector también se borran sus lecturas (como el CASCADE que tenía Room)
    void deleteBySectorId(Long sectorId);
}
