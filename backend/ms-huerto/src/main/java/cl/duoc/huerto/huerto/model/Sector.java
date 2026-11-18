package cl.duoc.huerto.huerto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Un SECTOR es una zona del huerto (por ejemplo "Huerta Norte" o "Invernadero").
// Las fechas se guardan como Long (milisegundos desde 1970), igual que en la app.
@Entity
@Table(name = "sectores")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 10)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60)
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200)
    private String descripcion;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 60)
    private String ubicacion;

    // Disponible, En uso, En mantenimiento o Fuera de uso
    @NotBlank
    private String estado;

    // id del docente responsable (vive en ms-usuarios). Puede ser null.
    private Long responsableId;

    @NotNull
    private Long fechaCreacion;

    @NotNull
    private Long fechaActualizacion;

    public Sector() {
    }

    public Sector(String codigo, String nombre, String descripcion, String ubicacion, String estado,
                  Long responsableId, Long fechaCreacion, Long fechaActualizacion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.responsableId = responsableId;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getResponsableId() { return responsableId; }
    public void setResponsableId(Long responsableId) { this.responsableId = responsableId; }

    public Long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Long getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Long fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
