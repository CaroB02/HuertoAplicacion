package cl.duoc.huerto.tareas.controller;

import cl.duoc.huerto.tareas.service.ProblemaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Endpoint INTERNO para otro microservicio (no lo usa la app):
// ms-huerto pregunta aquí si un sector o cultivo tiene tareas o reportes antes de borrarlo.
// GET /referencias?sectorId=1  ->  true / false
@RestController
public class ReferenciaController {

    private final ProblemaService service;

    public ReferenciaController(ProblemaService service) {
        this.service = service;
    }

    @GetMapping("/referencias")
    public boolean tieneReferencias(@RequestParam(required = false) Long sectorId,
                                    @RequestParam(required = false) Long cultivoId) {
        return service.tieneReferencias(sectorId, cultivoId);
    }
}
