package cl.duoc.huerto.tareas.repository;

import cl.duoc.huerto.tareas.model.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {

    // Las fotos de una tarea, la más reciente primero
    List<Evidencia> findByTareaIdOrderByFechaDesc(Long tareaId);

    // Al borrar una tarea se borran sus fotos (como el CASCADE que tenía Room)
    void deleteByTareaId(Long tareaId);
}
