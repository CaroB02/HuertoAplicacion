package cl.duoc.huerto.tareas.controller;

import cl.duoc.huerto.tareas.model.Validacion;
import cl.duoc.huerto.tareas.service.TareaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// GET /validaciones -> todas las revisiones (las usa el historial de actividades)
@RestController
public class ValidacionController {

    private final TareaService service;

    public ValidacionController(TareaService service) {
        this.service = service;
    }

    @GetMapping("/validaciones")
    public List<Validacion> listar() {
        return service.todasLasValidaciones();
    }
}
