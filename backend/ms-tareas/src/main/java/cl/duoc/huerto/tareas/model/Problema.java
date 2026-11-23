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

// Reporte de un problema del huerto (plaga, falta de agua...). El docente le da seguimiento.
@Entity
@Table(name = "problemas")
public class Problema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El sector es obligatorio")
    private Long sectorId;

    @NotNull(message = "El cultivo es obligatorio")
    private Long cultivoId;

    @NotBlank
    private String tipo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 300)
    @Column(length = 500)
    private String descripcion;

    @NotNull
    private Long fechaReporte;

    @NotNull
    private Long reportadoPorId;

    @Column(length = 500)
    private String fotoUri;

    // Informado, En revisión, Atendido o Cerrado
    @NotBlank
    private String estado;

    @Column(length = 500)
    private String comentarioDocente = "";

    // Se llena cuando pasa a Atendido o Cerrado
    private Long fechaCierre;

    public Problema() {
    }

    public Problema(Long sectorId, Long cultivoId, String tipo, String descripcion, Long fechaReporte,
                    Long reportadoPorId, String fotoUri, String estado, String comentarioDocente, Long fechaCierre) {
        this.sectorId = sectorId;
        this.cultivoId = cultivoId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaReporte = fechaReporte;
        this.reportadoPorId = reportadoPorId;
        this.fotoUri = fotoUri;
        this.estado = estado;
        this.comentarioDocente = comentarioDocente == null ? "" : comentarioDocente;
        this.fechaCierre = fechaCierre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }

    public Long getCultivoId() { return cultivoId; }
    public void setCultivoId(Long cultivoId) { this.cultivoId = cultivoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(Long fechaReporte) { this.fechaReporte = fechaReporte; }

    public Long getReportadoPorId() { return reportadoPorId; }
    public void setReportadoPorId(Long reportadoPorId) { this.reportadoPorId = reportadoPorId; }

    public String getFotoUri() { return fotoUri; }
    public void setFotoUri(String fotoUri) { this.fotoUri = fotoUri; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getComentarioDocente() { return comentarioDocente; }
    public void setComentarioDocente(String comentarioDocente) {
        this.comentarioDocente = comentarioDocente == null ? "" : comentarioDocente;
    }

    public Long getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(Long fechaCierre) { this.fechaCierre = fechaCierre; }
}
