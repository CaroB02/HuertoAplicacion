package cl.duoc.huerto.huerto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio del HUERTO (puerto 8082).
// Administra los sectores, los cultivos y las lecturas simuladas de sensores.
@SpringBootApplication
public class HuertoApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuertoApplication.class, args);
    }
}
