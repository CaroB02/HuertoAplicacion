package cl.duoc.huerto.huerto.controller;

import cl.duoc.huerto.huerto.model.Cultivo;
import cl.duoc.huerto.huerto.service.CultivoService;
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

// CRUD completo de cultivos.
@RestController
@RequestMapping("/cultivos")
public class CultivoController {

    private final CultivoService service;

    public CultivoController(CultivoService service) {
        this.service = service;
    }

    // GET /cultivos  o  GET /cultivos?sectorId=1
    @GetMapping
    public List<Cultivo> listar(@RequestParam(required = false) Long sectorId) {
        return service.listar(sectorId);
    }

    // GET /cultivos/1
    @GetMapping("/{id}")
    public Cultivo buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // POST /cultivos -> 201 Created (400 si el sector no existe o las fechas no cuadran)
    @PostMapping
    public ResponseEntity<Cultivo> crear(@Valid @RequestBody Cultivo cultivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(cultivo));
    }

    // PUT /cultivos/1
    @PutMapping("/{id}")
    public Cultivo actualizar(@PathVariable Long id, @Valid @RequestBody Cultivo cultivo) {
        return service.actualizar(id, cultivo);
    }

    // DELETE /cultivos/1 -> 204, o 409 si tiene tareas o reportes
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
