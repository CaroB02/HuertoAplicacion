package cl.duoc.huerto.huerto.controller;

import cl.duoc.huerto.huerto.model.LecturaSensor;
import cl.duoc.huerto.huerto.service.LecturaSensorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Lecturas simuladas de sensores.
@RestController
@RequestMapping("/lecturas")
public class LecturaSensorController {

    private final LecturaSensorService service;

    public LecturaSensorController(LecturaSensorService service) {
        this.service = service;
    }

    // GET /lecturas -> la más reciente primero
    @GetMapping
    public List<LecturaSensor> listar() {
        return service.listar();
    }

    // POST /lecturas -> registra una lectura simulada (se puede probar con Postman)
    @PostMapping
    public ResponseEntity<LecturaSensor> registrar(@Valid @RequestBody LecturaSensor lectura) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(lectura));
    }
}
