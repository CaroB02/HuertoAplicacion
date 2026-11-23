package cl.duoc.huerto.tareas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de TAREAS (puerto 8083).
// Administra las tareas, sus evidencias (fotos), las validaciones del docente y los reportes de problemas.
@SpringBootApplication
public class
TareasApplication {
    public static void main(String[] args) {
        SpringApplication.run(TareasApplication.class, args);
    }
}
