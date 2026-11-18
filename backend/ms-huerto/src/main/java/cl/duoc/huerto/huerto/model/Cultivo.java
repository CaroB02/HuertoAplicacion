package cl.duoc.huerto.huerto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Un CULTIVO es una planta dentro de un sector. Relación 1 a N: un sector tiene muchos cultivos.
// sectorId apunta a la tabla sectores de ESTE mismo microservicio; el servicio revisa que exista.
@Entity
@Table(name = "cultivos")
public class Cultivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60)
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200)
    private String descripcion;

    @NotNull(message = "El sector es obligatorio")
    private Long sectorId;

    @NotNull
    private Long fechaSiembra;

    // Opcional: no todos los cultivos se trasplantan
    private Long fechaTrasplante;

    // Fecha estimada de cosecha
    @NotNull
    private Long fechaCosecha;

    @NotNull
    @Min(value = 1, message = "La frecuencia de riego debe ser mayor a 0")
    private Integer frecuenciaRiegoDias;

    // Planificado, Sembrado, En crecimiento, Listo para cosechar, Cosechado, Con problemas, Finalizado
    @NotBlank
    private String estado;

    @Column(length = 500)
    private String observaciones = "";

    // Ruta de la foto referencial dentro del teléfono (puede ser null)
    @Column(length = 500)
    private String fotoUri;

    public Cultivo() {
    }

    public Cultivo(String nombre, String descripcion, Long sectorId, Long fechaSiembra, Long fechaTrasplante,
                   Long fechaCosecha, Integer frecuenciaRiegoDias, String estado, String observaciones, String fotoUri) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sectorId = sectorId;
        this.fechaSiembra = fechaSiembra;
        this.fechaTrasplante = fechaTrasplante;
        this.fechaCosecha = fechaCosecha;
        this.frecuenciaRiegoDias = frecuenciaRiegoDias;
        this.estado = estado;
        this.observaciones = observaciones == null ? "" : observaciones;
        this.fotoUri = fotoUri;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }

    public Long getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(Long fechaSiembra) { this.fechaSiembra = fechaSiembra; }

    public Long getFechaTrasplante() { return fechaTrasplante; }
    public void setFechaTrasplante(Long fechaTrasplante) { this.fechaTrasplante = fechaTrasplante; }

    public Long getFechaCosecha() { return fechaCosecha; }
    public void setFechaCosecha(Long fechaCosecha) { this.fechaCosecha = fechaCosecha; }

    public Integer getFrecuenciaRiegoDias() { return frecuenciaRiegoDias; }
    public void setFrecuenciaRiegoDias(Integer frecuenciaRiegoDias) { this.frecuenciaRiegoDias = frecuenciaRiegoDias; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones == null ? "" : observaciones; }

    public String getFotoUri() { return fotoUri; }
    public void setFotoUri(String fotoUri) { this.fotoUri = fotoUri; }
}
