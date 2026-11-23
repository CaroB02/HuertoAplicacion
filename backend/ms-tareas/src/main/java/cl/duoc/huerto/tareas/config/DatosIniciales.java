package cl.duoc.huerto.tareas.config;

import cl.duoc.huerto.tareas.model.Evidencia;
import cl.duoc.huerto.tareas.model.Problema;
import cl.duoc.huerto.tareas.model.Tarea;
import cl.duoc.huerto.tareas.model.Validacion;
import cl.duoc.huerto.tareas.repository.EvidenciaRepository;
import cl.duoc.huerto.tareas.repository.ProblemaRepository;
import cl.duoc.huerto.tareas.repository.TareaRepository;
import cl.duoc.huerto.tareas.repository.ValidacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Datos de prueba: una tarea en CADA estado para poder demostrar todos los flujos
// (pendiente, atrasada, en desarrollo, por validar, validada y rechazada) y dos reportes.
// Usa los id que crean ms-usuarios y ms-huerto en sus propios datos de prueba.
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final long MILIS_POR_DIA = 24L * 60 * 60 * 1000;
    private static final String FOTO_DEMO =
            "file:///data/user/0/com.example.huertoaplicaciones/files/fotos/foto_demo.png";

    // id de ms-usuarios
    private static final long DOCENTE = 1L, MATIAS = 2L, VALENTINA = 3L, BENJAMIN = 4L;
    // id de ms-huerto
    private static final long NORTE = 1L, INVERNADERO = 2L, HIERBAS = 3L;
    private static final long LECHUGA = 1L, ZANAHORIA = 2L, TOMATE = 3L, CILANTRO = 5L;

    private final TareaRepository tareaRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final ValidacionRepository validacionRepository;
    private final ProblemaRepository problemaRepository;

    public DatosIniciales(TareaRepository tareaRepository, EvidenciaRepository evidenciaRepository,
                          ValidacionRepository validacionRepository, ProblemaRepository problemaRepository) {
        this.tareaRepository = tareaRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.validacionRepository = validacionRepository;
        this.problemaRepository = problemaRepository;
    }

    @Override
    public void run(String... args) {
        if (tareaRepository.count() > 0) return;

        // Pendiente (próxima)
        tareaRepository.save(new Tarea("Riego", "Regar la lechuga",
                "Regar con regadera, sin mojar las hojas, hasta que la tierra esté húmeda.",
                NORTE, LECHUGA, dias(-2), dias(1), MATIAS, DOCENTE, "Pendiente"));

        // Pendiente y vencida: la app la muestra como "Atrasada"
        tareaRepository.save(new Tarea("Retiro de malezas", "Retirar malezas de la zanahoria",
                "Sacar las malezas con la mano, cuidando no arrancar las plántulas.",
                NORTE, ZANAHORIA, dias(-6), dias(-3), VALENTINA, DOCENTE, "Pendiente"));

        // En desarrollo
        tareaRepository.save(new Tarea("Observación del cultivo", "Observar el crecimiento del tomate",
                "Medir la altura de tres plantas y anotar el color de las hojas.",
                INVERNADERO, TOMATE, dias(-3), dias(2), BENJAMIN, DOCENTE, "En desarrollo"));

        // Pendiente de validación (con evidencia)
        Tarea porValidar = new Tarea("Riego", "Regar el tomate",
                "Regar en la base de la planta, temprano en la mañana.",
                INVERNADERO, TOMATE, dias(-4), dias(-1), MATIAS, DOCENTE, "Pendiente de validación");
        porValidar.setFechaRealizacion(dias(-1));
        porValidar.setComentarioEstudiante("Regué las seis plantas del invernadero.");
        Long idPorValidar = tareaRepository.save(porValidar).getId();
        evidenciaRepository.save(new Evidencia(idPorValidar, FOTO_DEMO, dias(-1), MATIAS));

        // Validada (con evidencia y registro de validación)
        Long idValidada = guardarRevisada(new Tarea("Siembra", "Sembrar zanahoria",
                        "Sembrar a 1 cm de profundidad con 5 cm entre semillas.",
                        NORTE, ZANAHORIA, dias(-14), dias(-10), VALENTINA, DOCENTE, "Validada"),
                dias(-10), "Sembramos dos hileras.", "Muy buen trabajo.", dias(-9));
        evidenciaRepository.save(new Evidencia(idValidada, FOTO_DEMO, dias(-10), VALENTINA));
        validacionRepository.save(new Validacion(idValidada, DOCENTE, "Validada", "Muy buen trabajo.", dias(-9)));

        // Riego validado: sirve para calcular el próximo riego de la lechuga
        Long idRiego = guardarRevisada(new Tarea("Riego", "Regar la lechuga",
                        "Regar hasta que la tierra esté húmeda.",
                        NORTE, LECHUGA, dias(-6), dias(-3), MATIAS, DOCENTE, "Validada"),
                dias(-3), "Listo, tierra húmeda.", "Correcto.", dias(-3));
        evidenciaRepository.save(new Evidencia(idRiego, FOTO_DEMO, dias(-3), MATIAS));
        validacionRepository.save(new Validacion(idRiego, DOCENTE, "Validada", "Correcto.", dias(-3)));

        // Rechazada: el estudiante debe corregir y reenviar
        String motivo = "Faltó limpiar la maceta del fondo. Corrígelo y vuelve a enviar.";
        Long idRechazada = guardarRevisada(new Tarea("Limpieza", "Limpiar las macetas del cilantro",
                        "Retirar hojas secas y restos de tierra alrededor de las macetas.",
                        HIERBAS, CILANTRO, dias(-7), dias(-4), BENJAMIN, DOCENTE, "Rechazada"),
                dias(-4), "Limpié las macetas.", motivo, dias(-3));
        evidenciaRepository.save(new Evidencia(idRechazada, FOTO_DEMO, dias(-4), BENJAMIN));
        validacionRepository.save(new Validacion(idRechazada, DOCENTE, "Rechazada", motivo, dias(-3)));

        // ---------- Reportes de problemas: uno abierto y uno cerrado ----------
        problemaRepository.save(new Problema(HIERBAS, CILANTRO, "Plaga simulada",
                "Se ven manchas y pequeños bichos en las hojas del cilantro.",
                dias(-2), BENJAMIN, FOTO_DEMO, "Informado", "", null));
        problemaRepository.save(new Problema(INVERNADERO, TOMATE, "Falta de agua",
                "La tierra del tomate estaba muy seca por la mañana.",
                dias(-8), MATIAS, null, "Cerrado",
                "Se ajustó la frecuencia de riego y se revisó el goteo.", dias(-6)));
    }

    // Completa los datos de entrega y revisión de una tarea ya revisada y la guarda
    private Long guardarRevisada(Tarea tarea, long fechaRealizacion, String comentarioEstudiante,
                                 String comentarioDocente, long fechaValidacion) {
        tarea.setFechaRealizacion(fechaRealizacion);
        tarea.setComentarioEstudiante(comentarioEstudiante);
        tarea.setComentarioDocente(comentarioDocente);
        tarea.setValidadoPorId(DOCENTE);
        tarea.setFechaValidacion(fechaValidacion);
        return tareaRepository.save(tarea).getId();
    }

    private static long dias(int n) {
        return System.currentTimeMillis() + n * MILIS_POR_DIA;
    }
}
