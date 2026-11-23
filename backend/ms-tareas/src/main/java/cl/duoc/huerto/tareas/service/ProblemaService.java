package cl.duoc.huerto.tareas.service;

import cl.duoc.huerto.tareas.model.Problema;
import cl.duoc.huerto.tareas.repository.ProblemaRepository;
import cl.duoc.huerto.tareas.repository.TareaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Reglas de negocio de los reportes de problemas.
@Service
public class ProblemaService {

    private final ProblemaRepository problemaRepository;
    private final TareaRepository tareaRepository;

    public ProblemaService(ProblemaRepository problemaRepository, TareaRepository tareaRepository) {
        this.problemaRepository = problemaRepository;
        this.tareaRepository = tareaRepository;
    }

    public List<Problema> listar(Long usuarioId) {
        if (usuarioId != null) return problemaRepository.findByReportadoPorIdOrderByFechaReporteDesc(usuarioId);
        return problemaRepository.findAllByOrderByFechaReporteDesc();
    }

    public Problema buscar(Long id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado"));
    }

    // Todo reporte nuevo parte como "Informado"
    public Problema crear(Problema problema) {
        problema.setId(null);
        problema.setEstado("Informado");
        return problemaRepository.save(problema);
    }

    // El docente cambia el estado de atención. Atender o cerrar exige un comentario.
    public Problema actualizar(Long id, Problema datos) {
        buscar(id);
        boolean exigeComentario = "Atendido".equals(datos.getEstado()) || "Cerrado".equals(datos.getEstado());
        if (exigeComentario && datos.getComentarioDocente().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Explica en un comentario cómo se atendió el problema");
        }
        datos.setId(id);
        return problemaRepository.save(datos);
    }

    // ¿Hay tareas o reportes que apunten a este sector o cultivo? Lo pregunta ms-huerto antes de borrar.
    public boolean tieneReferencias(Long sectorId, Long cultivoId) {
        if (sectorId != null && (tareaRepository.existsBySectorId(sectorId) || problemaRepository.existsBySectorId(sectorId))) {
            return true;
        }
        return cultivoId != null
                && (tareaRepository.existsByCultivoId(cultivoId) || problemaRepository.existsByCultivoId(cultivoId));
    }
}
