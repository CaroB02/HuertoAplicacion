package cl.duoc.huerto.usuarios.controller;

import cl.duoc.huerto.usuarios.dto.LoginRequest;
import cl.duoc.huerto.usuarios.model.Usuario;
import cl.duoc.huerto.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CONTROLADOR: expone los endpoints HTTP que consume la app con Retrofit (UsuariosApi.kt).
// Todas las rutas empiezan con /usuarios.
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // GET /usuarios -> todos los usuarios (para mostrar nombres en tareas, historial, etc.)
    @GetMapping
    public List<Usuario> listar() {
        return service.listar();
    }

    // GET /usuarios/estudiantes -> solo estudiantes (para asignar tareas)
    @GetMapping("/estudiantes")
    public List<Usuario> listarEstudiantes() {
        return service.listarEstudiantes();
    }

    // GET /usuarios/5 -> un usuario (404 si no existe)
    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // GET /usuarios/buscar?correo=x@huerta.cl -> 404 si no existe (lo usa el registro)
    @GetMapping("/buscar")
    public Usuario buscarPorCorreo(@RequestParam String correo) {
        return service.buscarPorCorreo(correo);
    }

    // POST /usuarios -> crear cuenta. Responde 201 Created, o 409 si el correo ya existe.
    // @Valid revisa las anotaciones de Usuario (@NotBlank, @Email...) y responde 400 si fallan.
    @PostMapping
    public ResponseEntity<Usuario> registrar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(usuario));
    }

    // POST /usuarios/login -> 200 con el usuario, o 401 si el correo o la clave no coinciden
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@Valid @RequestBody LoginRequest login) {
        return service.iniciarSesion(login.correo(), login.clave())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
