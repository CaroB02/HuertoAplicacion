package cl.duoc.huerto.usuarios.repository;

import cl.duoc.huerto.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// REPOSITORIO: Spring Data JPA escribe el SQL a partir del NOMBRE de cada método.
// JpaRepository ya trae findAll, findById, save, deleteById, count...
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // SELECT * FROM usuarios WHERE correo = ?
    Optional<Usuario> findByCorreo(String correo);

    // SELECT * FROM usuarios WHERE correo = ? AND clave = ? AND activo = true
    Optional<Usuario> findByCorreoAndClaveAndActivoTrue(String correo, String clave);

    // SELECT * FROM usuarios WHERE perfil = ?
    List<Usuario> findByPerfil(String perfil);

    boolean existsByCorreo(String correo);
}
