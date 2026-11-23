package cl.duoc.huerto.tareas.controller;

import cl.duoc.huerto.tareas.dto.EntregaRequest;
import cl.duoc.huerto.tareas.dto.ValidacionRequest;
import cl.duoc.huerto.tareas.model.Evidencia;
import cl.duoc.huerto.tareas.model.Tarea;
import cl.duoc.huerto.tareas.model.Validacion;
import cl.duoc.huerto.tareas.service.TareaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoints de las tareas. Los consume TareasApi.kt en la app.
@RestController
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService service;

    public TareaController(TareaService service) {
        this.service = service;
    }

    // GET /tareas  o  GET /tareas?estudianteId=2
    @GetMapping
    public List<Tarea> listar(@RequestParam(required = false) Long estudianteId) {
        return service.listar(estudianteId);
    }

    // GET /tareas/1
    @GetMapping("/{id}")
    public Tarea buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // POST /tareas -> 201 Created (siempre parte en "Pendiente")
    @PostMapping
    public ResponseEntity<Tarea> crear(@Valid @RequestBody Tarea tarea) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(tarea));
    }

    // PUT /tareas/1 -> editar o cambiar de estado (por ejemplo, iniciar la tarea)
    @PutMapping("/{id}")
    public Tarea actualizar(@PathVariable Long id, @Valid @RequestBody Tarea tarea) {
        return service.actualizar(id, tarea);
    }

    // DELETE /tareas/1 -> 204, o 409 si ya se inició
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // POST /tareas/1/entrega -> el estudiante entrega con comentario y fotos
    @PostMapping("/{id}/entrega")
    public Tarea entregar(@PathVariable Long id, @Valid @RequestBody EntregaRequest entrega) {
        return service.entregar(id, entrega);
    }

    // POST /tareas/1/validaciones -> el docente valida o rechaza
    @PostMapping("/{id}/validaciones")
    public Tarea validar(@PathVariable Long id, @Valid @RequestBody ValidacionRequest revision) {
        return service.registrarValidacion(id, revision);
    }

    // GET /tareas/1/evidencias -> las fotos de la tarea
    @GetMapping("/{id}/evidencias")
    public List<Evidencia> evidencias(@PathVariable Long id) {
        return service.evidenciasDe(id);
    }

    // GET /tareas/1/validaciones -> historial de revisiones de la tarea
    @GetMapping("/{id}/validaciones")
    public List<Validacion> validaciones(@PathVariable Long id) {
        return service.validacionesDe(id);
    }
}
