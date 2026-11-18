package cl.duoc.huerto.huerto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Medición de humedad y temperatura de un sector.
// OJO: son datos SIMULADOS (no hay sensores reales), por eso "origen" dice "simulación académica".
@Entity
@Table(name = "lecturas_sensor")
public class LecturaSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long sectorId;

    @NotNull
    private Long fecha;

    @NotNull
    private Integer humedadSuelo;       // porcentaje

    @NotNull
    private Double temperatura;         // grados Celsius

    @NotNull
    private Integer humedadAmbiente;    // porcentaje

    private String origen = "simulación académica";

    // Normal, Bajo, Alto o Pendiente de revisión
    @NotBlank
    private String estado;

    public LecturaSensor() {
    }

    public LecturaSensor(Long sectorId, Long fecha, Integer humedadSuelo, Double temperatura,
                         Integer humedadAmbiente, String estado) {
        this.sectorId = sectorId;
        this.fecha = fecha;
        this.humedadSuelo = humedadSuelo;
        this.temperatura = temperatura;
        this.humedadAmbiente = humedadAmbiente;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }

    public Long getFecha() { return fecha; }
    public void setFecha(Long fecha) { this.fecha = fecha; }

    public Integer getHumedadSuelo() { return humedadSuelo; }
    public void setHumedadSuelo(Integer humedadSuelo) { this.humedadSuelo = humedadSuelo; }

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double temperatura) { this.temperatura = temperatura; }

    public Integer getHumedadAmbiente() { return humedadAmbiente; }
    public void setHumedadAmbiente(Integer humedadAmbiente) { this.humedadAmbiente = humedadAmbiente; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen == null ? "simulación académica" : origen; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
