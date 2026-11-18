package cl.duoc.huerto.usuarios.service;

import cl.duoc.huerto.usuarios.model.Usuario;
import cl.duoc.huerto.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

// SERVICIO: aquí van las reglas de negocio. El controlador solo recibe la petición y llama al servicio.
@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    // Inyección por constructor: Spring entrega el repositorio automáticamente
    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public List<Usuario> listarEstudiantes() {
        return repository.findByPerfil("ESTUDIANTE");
    }

    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario buscarPorCorreo(String correo) {
        return repository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    // Registro: el correo se guarda en minúscula y no puede repetirse (409 Conflict)
    public Usuario registrar(Usuario usuario) {
        String correo = usuario.getCorreo().trim().toLowerCase();
        if (repository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }
        if (!usuario.getPerfil().equals("ADMIN") && !usuario.getPerfil().equals("ESTUDIANTE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil no válido");
        }
        usuario.setId(null);          // el id lo genera la base de datos
        usuario.setCorreo(correo);
        usuario.setActivo(true);
        return repository.save(usuario);
    }

    // Login: devuelve el usuario si el correo y la clave coinciden y la cuenta está activa
    public Optional<Usuario> iniciarSesion(String correo, String clave) {
        return repository.findByCorreoAndClaveAndActivoTrue(correo.trim().toLowerCase(), clave);
    }
}
