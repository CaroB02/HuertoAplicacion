package cl.duoc.huerto.tareas.repository;

import cl.duoc.huerto.tareas.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    // Todas las tareas, las más próximas primero
    List<Tarea> findAllByOrderByFechaProgramadaAsc();

    // Solo las de un estudiante (privilegio mínimo: cada uno ve las suyas)
    List<Tarea> findByAsignadoAIdOrderByFechaProgramadaAsc(Long estudianteId);

    // Las usa GET /referencias para que ms-huerto sepa si puede borrar un sector o cultivo
    boolean existsBySectorId(Long sectorId);
    boolean existsByCultivoId(Long cultivoId);
}
