package cl.duoc.huerto.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de USUARIOS (puerto 8081).
// Se encarga de las cuentas: registro, inicio de sesión y listado de docentes y estudiantes.
@SpringBootApplication
public class UsuariosApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }
}
