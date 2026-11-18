package cl.duoc.huerto.huerto.controller;

import cl.duoc.huerto.huerto.model.Sector;
import cl.duoc.huerto.huerto.service.SectorService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CRUD completo de sectores. Lo consume HuertoApi.kt en la app.
@RestController
@RequestMapping("/sectores")
public class SectorController {

    private final SectorService service;

    public SectorController(SectorService service) {
        this.service = service;
    }

    // GET /sectores
    @GetMapping
    public List<Sector> listar() {
        return service.listar();
    }

    // GET /sectores/1
    @GetMapping("/{id}")
    public Sector buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    // POST /sectores -> 201 Created
    @PostMapping
    public ResponseEntity<Sector> crear(@Valid @RequestBody Sector sector) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(sector));
    }

    // PUT /sectores/1
    @PutMapping("/{id}")
    public Sector actualizar(@PathVariable Long id, @Valid @RequestBody Sector sector) {
        return service.actualizar(id, sector);
    }

    // DELETE /sectores/1 -> 204 No Content, o 409 si tiene cultivos, tareas o reportes
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
