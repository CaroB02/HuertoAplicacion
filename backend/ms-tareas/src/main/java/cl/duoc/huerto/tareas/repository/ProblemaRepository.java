package cl.duoc.huerto.tareas.repository;

import cl.duoc.huerto.tareas.model.Problema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemaRepository extends JpaRepository<Problema, Long> {

    // Todos los reportes (los ve el docente), el más reciente primero
    List<Problema> findAllByOrderByFechaReporteDesc();

    // Solo los reportes de un usuario (cada estudiante ve los suyos)
    List<Problema> findByReportadoPorIdOrderByFechaReporteDesc(Long usuarioId);

    boolean existsBySectorId(Long sectorId);
    boolean existsByCultivoId(Long cultivoId);
}
