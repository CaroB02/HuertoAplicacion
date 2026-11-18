package cl.duoc.huerto.huerto.config;

import cl.duoc.huerto.huerto.model.Cultivo;
import cl.duoc.huerto.huerto.model.LecturaSensor;
import cl.duoc.huerto.huerto.model.Sector;
import cl.duoc.huerto.huerto.repository.CultivoRepository;
import cl.duoc.huerto.huerto.repository.LecturaSensorRepository;
import cl.duoc.huerto.huerto.repository.SectorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Datos de prueba del huerto. Solo se cargan si la tabla de sectores está vacía.
// Las fechas son RELATIVAS a hoy (dias(-3) = hace 3 días) para que siempre se vean actuales.
// Orden de los id que usa ms-tareas: sectores 1 Norte, 2 Invernadero, 3 Hierbas;
// cultivos 1 Lechuga, 2 Zanahoria, 3 Tomate, 4 Albahaca, 5 Cilantro. El docente es el usuario 1.
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final long MILIS_POR_DIA = 24L * 60 * 60 * 1000;

    // Ruta de la foto de ejemplo dentro del teléfono: la app la crea al arrancar (FotoUtils.crearFotoDemo)
    public static final String FOTO_DEMO =
            "file:///data/user/0/com.example.huertoaplicaciones/files/fotos/foto_demo.png";

    private final SectorRepository sectorRepository;
    private final CultivoRepository cultivoRepository;
    private final LecturaSensorRepository lecturaRepository;

    public DatosIniciales(SectorRepository sectorRepository, CultivoRepository cultivoRepository,
                          LecturaSensorRepository lecturaRepository) {
        this.sectorRepository = sectorRepository;
        this.cultivoRepository = cultivoRepository;
        this.lecturaRepository = lecturaRepository;
    }

    @Override
    public void run(String... args) {
        if (sectorRepository.count() > 0) return;

        long docente = 1L;

        // ---------- Sectores ----------
        Long norte = sectorRepository.save(new Sector("S-01", "Huerta Norte", "Camas de cultivo al aire libre",
                "Patio norte, junto al gimnasio", "En uso", docente, dias(-60), dias(-5))).getId();
        Long invernadero = sectorRepository.save(new Sector("S-02", "Invernadero",
                "Estructura cubierta para cultivos sensibles", "Costado de la biblioteca", "En uso",
                docente, dias(-60), dias(-10))).getId();
        Long hierbas = sectorRepository.save(new Sector("S-03", "Jardín de hierbas",
                "Macetas y camas pequeñas de hierbas aromáticas", "Entrada del colegio", "Disponible",
                null, dias(-45), dias(-45))).getId();

        // ---------- Cultivos ----------
        cultivoRepository.save(new Cultivo("Lechuga", "Lechuga costina para ensaladas", norte,
                dias(-20), dias(-10), dias(5), 2, "En crecimiento", "Crece bien, hojas firmes", FOTO_DEMO));
        cultivoRepository.save(new Cultivo("Zanahoria", "Zanahoria orgánica", norte,
                dias(-10), null, dias(50), 4, "Sembrado", "", null));
        cultivoRepository.save(new Cultivo("Tomate cherry", "Tomate cherry con tutor", invernadero,
                dias(-40), dias(-25), dias(25), 3, "En crecimiento", "", FOTO_DEMO));
        cultivoRepository.save(new Cultivo("Albahaca", "Albahaca para el taller de cocina", hierbas,
                dias(5), null, dias(60), 2, "Planificado", "", null));
        cultivoRepository.save(new Cultivo("Cilantro", "Cilantro en maceta", hierbas,
                dias(-15), null, dias(20), 2, "Con problemas", "Hojas con manchas", null));

        // ---------- Lecturas simuladas ----------
        // La del invernadero está en "Bajo" a propósito, para que el panel muestre el aviso
        lecturaRepository.save(new LecturaSensor(norte, dias(0) - 3_600_000L, 62, 21.5, 55, "Normal"));
        lecturaRepository.save(new LecturaSensor(invernadero, dias(0) - 1_800_000L, 48, 27.8, 70, "Bajo"));
        lecturaRepository.save(new LecturaSensor(hierbas, dias(0) - 7_200_000L, 71, 19.2, 60, "Normal"));
    }

    private static long dias(int n) {
        return System.currentTimeMillis() + n * MILIS_POR_DIA;
    }
}
