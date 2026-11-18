package cl.duoc.huerto.usuarios.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ENTIDAD: Hibernate crea la tabla "usuarios" a partir de esta clase.
// Los nombres de los atributos son los mismos que usa la app (data class Usuario en Kotlin),
// así Gson puede convertir el JSON en objetos sin configuración extra.
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60)
    private String nombre;

    // unique = true: no pueden existir dos cuentas con el mismo correo
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Column(unique = true, nullable = false)
    private String correo;

    // WRITE_ONLY: la clave se RECIBE al registrar, pero nunca se DEVUELVE en las respuestas JSON.
    // Proyecto académico: en una app real se guardaría cifrada (por ejemplo con BCrypt).
    @NotBlank(message = "La clave es obligatoria")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String clave;

    // "ADMIN" (docente) o "ESTUDIANTE"
    @NotBlank(message = "El perfil es obligatorio")
    private String perfil;

    private String grupo = "";

    // Si activo = false la persona no puede iniciar sesión ni recibir tareas
    private boolean activo = true;

    // JPA necesita un constructor vacío
    public Usuario() {
    }

    public Usuario(String nombre, String correo, String clave, String perfil, String grupo) {
        this.nombre = nombre;
        this.correo = correo;
        this.clave = clave;
        this.perfil = perfil;
        this.grupo = grupo;
        this.activo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo == null ? "" : grupo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
