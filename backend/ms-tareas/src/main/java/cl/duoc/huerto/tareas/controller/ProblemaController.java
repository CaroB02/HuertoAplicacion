package cl.duoc.huerto.tareas.controller;

import cl.duoc.huerto.tareas.model.Problema;
import cl.duoc.huerto.tareas.service.ProblemaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Reportes de problemas del huerto.
@RestController
@RequestMapping("/problemas")
public class ProblemaController {

    private final ProblemaService service;

    public ProblemaController(ProblemaService service) {
        this.service = service;
    }

    // GET /problemas  o  GET /problemas?usuarioId=4
    @GetMapping
    public List<Problema> listar(@RequestParam(required = false) Long usuarioId) {
        return service.listar(usuarioId);
    }

    // GET /problemas/1
    @GetMapping("/{id}")
    public Problema buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // POST /problemas -> 201 Created (parte como "Informado")
    @PostMapping
    public ResponseEntity<Problema> crear(@Valid @RequestBody Problema problema) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(problema));
    }

    // PUT /problemas/1 -> el docente cambia el estado de atención
    @PutMapping("/{id}")
    public Problema actualizar(@PathVariable Long id, @Valid @RequestBody Problema problema) {
        return service.actualizar(id, problema);
    }
}
