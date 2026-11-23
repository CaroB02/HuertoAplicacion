package cl.duoc.huerto.tareas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Una TAREA es un trabajo del huerto que el docente crea y asigna a un estudiante.
// sectorId y cultivoId viven en ms-huerto; asignadoAId, creadoPorId y validadoPorId en ms-usuarios.
// Flujo: Pendiente -> En desarrollo -> Pendiente de validación -> Validada o Rechazada.
@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Riego, Siembra, Mantenimiento...
    @NotBlank
    private String tipo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200)
    private String descripcion;

    @NotBlank(message = "Las instrucciones son obligatorias")
    @Size(max = 300)
    @Column(length = 500)
    private String instrucciones;

    @NotNull
    private Long sectorId;

    @NotNull
    private Long cultivoId;

    @NotNull
    private Long fechaCreacion;

    // Día en que debe hacerse (de aquí la app calcula "Atrasada")
    @NotNull
    private Long fechaProgramada;

    // Estudiante responsable
    @NotNull
    private Long asignadoAId;

    // Docente que la creó
    @NotNull
    private Long creadoPorId;

    @NotBlank
    private String estado;

    private Long fechaRealizacion;

    @Column(length = 500)
    private String comentarioEstudiante = "";

    @Column(length = 500)
    private String comentarioDocente = "";

    private Long validadoPorId;

    private Long fechaValidacion;

    public Tarea() {
    }

    public Tarea(String tipo, String descripcion, String instrucciones, Long sectorId, Long cultivoId,
                 Long fechaCreacion, Long fechaProgramada, Long asignadoAId, Long creadoPorId, String estado) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.instrucciones = instrucciones;
        this.sectorId = sectorId;
        this.cultivoId = cultivoId;
        this.fechaCreacion = fechaCreacion;
        this.fechaProgramada = fechaProgramada;
        this.asignadoAId = asignadoAId;
        this.creadoPorId = creadoPorId;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }

    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }

    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }

    public Long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Long getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(Long fechaProgramada) { this.fechaProgramada = fechaProgramada; }

    public Long getAsignadoAId() { return asignadoAId; }
    public void setAsignadoAId(Long asignadoAId) { this.asignadoAId = asignadoAId; }

    public Long getCreadoPorId() { return creadoPorId; }
    public void setCreadoPorId(Long creadoPorId) { this.creadoPorId = creadoPorId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(Long fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }

    public String getComentarioEstudiante() { return comentarioEstudiante; }
    public void setComentarioEstudiante(String comentarioEstudiante) {
        this.comentarioEstudiante = comentarioEstudiante == null ? "" : comentarioEstudiante;
    }

    public String getComentarioDocente() { return comentarioDocente; }
    public void setComentarioDocente(String comentarioDocente) {
        this.comentarioDocente = comentarioDocente == null ? "" : comentarioDocente;
    }

    public Long getValidadoPorId() { return validadoPorId; }
    public void setValidadoPorId(Long validadoPorId) { this.validadoPorId = validadoPorId; }

    public Long getFechaValidacion() { return fechaValidacion; }
    public void setFechaValidacion(Long fechaValidacion) { this.fechaValidacion = fechaValidacion; }
}
