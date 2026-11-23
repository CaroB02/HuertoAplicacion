package cl.duoc.huerto.tareas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Cada revisión del docente queda registrada: quién, cuándo y con qué resultado.
// Es un historial: solo se agregan filas, nunca se modifican.
@Entity
@Table(name = "validaciones")
public class Validacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tareaId;

    private Long validadorId;

    // "Validada" o "Rechazada"
    private String resultado;

    @Column(length = 500)
    private String comentario = "";

    private Long fecha;

    public Validacion() {
    }

    public Validacion(Long tareaId, Long validadorId, String resultado, String comentario, Long fecha) {
        this.tareaId = tareaId;
        this.validadorId = validadorId;
        this.resultado = resultado;
        this.comentario = comentario == null ? "" : comentario;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTareaId() { return tareaId; }
    public void setTareaId(Long tareaId) { this.tareaId = tareaId; }

    public Long getValidadorId() { return validadorId; }
    public void setValidadorId(Long validadorId) { this.validadorId = validadorId; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario == null ? "" : comentario; }

    public Long getFecha() { return fecha; }
    public void setFecha(Long fecha) { this.fecha = fecha; }
}
