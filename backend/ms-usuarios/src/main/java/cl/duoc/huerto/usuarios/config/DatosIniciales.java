package cl.duoc.huerto.usuarios.config;

import cl.duoc.huerto.usuarios.model.Usuario;
import cl.duoc.huerto.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Carga las cuentas de prueba la PRIMERA vez que arranca el microservicio (si la tabla está vacía).
// Los nombres son inventados: el caso exige trabajar solo con datos ficticios.
// IMPORTANTE: el orden define los id (1 = docente, 2, 3 y 4 = estudiantes).
// ms-huerto y ms-tareas usan esos mismos id en sus datos de prueba.
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository repository;

    public DatosIniciales(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        repository.save(new Usuario("Carolina Muñoz Soto", "carolina.munoz@huerta.cl", "123456", "ADMIN", "Taller Huerto"));
        repository.save(new Usuario("Matías Fuentes Rojas", "matias.fuentes@huerta.cl", "123456", "ESTUDIANTE", "Grupo A"));
        repository.save(new Usuario("Valentina Pérez Díaz", "valentina.perez@huerta.cl", "123456", "ESTUDIANTE", "Grupo B"));
        repository.save(new Usuario("Benjamín Castro Vera", "benjamin.castro@huerta.cl", "123456", "ESTUDIANTE", "Grupo C"));
    }
}
