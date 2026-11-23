package cl.duoc.huerto.tareas.service;

import cl.duoc.huerto.tareas.dto.EntregaRequest;
import cl.duoc.huerto.tareas.dto.ValidacionRequest;
import cl.duoc.huerto.tareas.model.Evidencia;
import cl.duoc.huerto.tareas.model.Tarea;
import cl.duoc.huerto.tareas.model.Validacion;
import cl.duoc.huerto.tareas.repository.EvidenciaRepository;
import cl.duoc.huerto.tareas.repository.TareaRepository;
import cl.duoc.huerto.tareas.repository.ValidacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Reglas de negocio de las tareas. Repite en el servidor las reglas que la app ya valida,
// porque un backend nunca debe confiar solo en lo que revisa el cliente.
@Service
public class TareaService {

    // Los mismos textos que usa la app en util/Constantes.kt (EstadoTarea)
    public static final String PENDIENTE = "Pendiente";
    public static final String EN_DESARROLLO = "En desarrollo";
    public static final String PENDIENTE_VALIDACION = "Pendiente de validación";
    public static final String VALIDADA = "Validada";
    public static final String RECHAZADA = "Rechazada";

    private final TareaRepository tareaRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final ValidacionRepository validacionRepository;

    public TareaService(TareaRepository tareaRepository, EvidenciaRepository evidenciaRepository,
                        ValidacionRepository validacionRepository) {
        this.tareaRepository = tareaRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.validacionRepository = validacionRepository;
    }

    public List<Tarea> listar(Long estudianteId) {
        if (estudianteId != null) return tareaRepository.findByAsignadoAIdOrderByFechaProgramadaAsc(estudianteId);
        return tareaRepository.findAllByOrderByFechaProgramadaAsc();
    }

    public Tarea buscar(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarea no encontrada"));
    }

    // Toda tarea nueva parte en "Pendiente"
    public Tarea crear(Tarea tarea) {
        tarea.setId(null);
        tarea.setEstado(PENDIENTE);
        return tareaRepository.save(tarea);
    }

    public Tarea actualizar(Long id, Tarea datos) {
        buscar(id);
        datos.setId(id);
        return tareaRepository.save(datos);
    }

    // Solo se eliminan tareas que no se han iniciado. Sus fotos y revisiones se borran con ella.
    // @Transactional: o se borra TODO o no se borra NADA.
    @Transactional
    public void eliminar(Long id) {
        Tarea tarea = buscar(id);
        if (!PENDIENTE.equals(tarea.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden eliminar tareas pendientes");
        }
        evidenciaRepository.deleteByTareaId(id);
        validacionRepository.deleteByTareaId(id);
        tareaRepository.deleteById(id);
    }

    // ENTREGA del estudiante: cambia el estado y guarda sus fotos en UNA transacción.
    // Así no puede quedar una tarea "entregada" sin sus fotos si algo falla a la mitad.
    @Transactional
    public Tarea entregar(Long id, EntregaRequest entrega) {
        Tarea tarea = buscar(id);
        if (!tarea.getAsignadoAId().equals(entrega.estudianteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el estudiante asignado puede entregarla");
        }
        if (!EN_DESARROLLO.equals(tarea.getEstado()) && !RECHAZADA.equals(tarea.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se entregan tareas en desarrollo o rechazadas");
        }
        tarea.setEstado(PENDIENTE_VALIDACION);
        tarea.setFechaRealizacion(entrega.fechaRealizacion());
        tarea.setComentarioEstudiante(entrega.comentario().trim());
        Tarea guardada = tareaRepository.save(tarea);

        // Una fila en evidencias por cada foto, enlazada a la tarea
        for (String uri : entrega.fotos()) {
            evidenciaRepository.save(new Evidencia(id, uri, entrega.fechaRealizacion(), entrega.estudianteId()));
        }
        return guardada;
    }

    // REVISIÓN del docente: actualiza la tarea y deja el registro en el historial, en UNA transacción.
    @Transactional
    public Tarea registrarValidacion(Long id, ValidacionRequest revision) {
        Tarea tarea = buscar(id);
        String resultado = revision.resultado();
        String comentario = revision.comentario() == null ? "" : revision.comentario().trim();

        if (!VALIDADA.equals(resultado) && !RECHAZADA.equals(resultado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El resultado debe ser Validada o Rechazada");
        }
        if (!PENDIENTE_VALIDACION.equals(tarea.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La tarea no está pendiente de validación");
        }
        // Nadie aprueba su propio trabajo
        if (tarea.getAsignadoAId().equals(revision.validadorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede validar la propia tarea");
        }
        // Rechazar exige explicar el motivo
        if (RECHAZADA.equals(resultado) && comentario.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para rechazar se debe explicar el motivo");
        }

        tarea.setEstado(resultado);
        tarea.setComentarioDocente(comentario);
        tarea.setValidadoPorId(revision.validadorId());
        tarea.setFechaValidacion(revision.fecha());
        Tarea guardada = tareaRepository.save(tarea);

        validacionRepository.save(new Validacion(id, revision.validadorId(), resultado, comentario, revision.fecha()));
        return guardada;
    }

    public List<Evidencia> evidenciasDe(Long tareaId) {
        return evidenciaRepository.findByTareaIdOrderByFechaDesc(tareaId);
    }

    public List<Validacion> validacionesDe(Long tareaId) {
        return validacionRepository.findByTareaIdOrderByFechaDesc(tareaId);
    }

    public List<Validacion> todasLasValidaciones() {
        return validacionRepository.findAllByOrderByFechaDesc();
    }
}
