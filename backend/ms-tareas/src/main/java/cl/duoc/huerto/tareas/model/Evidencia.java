package cl.duoc.huerto.tareas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Fotografía que un estudiante adjunta al entregar una tarea (una tarea puede tener varias: 1 a N).
@Entity
@Table(name = "evidencias")
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tareaId;

    // Ruta del archivo de la foto dentro del teléfono que la tomó
    @Column(length = 500)
    private String uri;

    private Long fecha;

    private Long subidaPorId;

    public Evidencia() {
    }

    public Evidencia(Long tareaId, String uri, Long fecha, Long subidaPorId) {
        this.tareaId = tareaId;
        this.uri = uri;
        this.fecha = fecha;
        this.subidaPorId = subidaPorId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTareaId() { return tareaId; }
    public void setTareaId(Long tareaId) { this.tareaId = tareaId; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public Long getFecha() { return fecha; }
    public void setFecha(Long fecha) { this.fecha = fecha; }

    public Long getSubidaPorId() { return subidaPorId; }
    public void setSubidaPorId(Long subidaPorId) { this.subidaPorId = subidaPorId; }
}
