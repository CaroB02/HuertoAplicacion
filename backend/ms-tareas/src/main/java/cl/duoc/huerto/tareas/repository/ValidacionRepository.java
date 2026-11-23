package cl.duoc.huerto.tareas.repository;

import cl.duoc.huerto.tareas.model.Validacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidacionRepository extends JpaRepository<Validacion, Long> {

    // Todas las revisiones (las usa el historial), la más reciente primero
    List<Validacion> findAllByOrderByFechaDesc();

    // Las revisiones de una tarea (detalle de la tarea)
    List<Validacion> findByTareaIdOrderByFechaDesc(Long tareaId);

    void deleteByTareaId(Long tareaId);
}
