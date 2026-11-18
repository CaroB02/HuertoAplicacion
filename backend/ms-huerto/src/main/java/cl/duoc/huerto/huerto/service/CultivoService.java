package cl.duoc.huerto.huerto.service;

import cl.duoc.huerto.huerto.model.Cultivo;
import cl.duoc.huerto.huerto.repository.CultivoRepository;
import cl.duoc.huerto.huerto.repository.SectorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Reglas de negocio de los cultivos.
@Service
public class CultivoService {

    private final CultivoRepository cultivoRepository;
    private final SectorRepository sectorRepository;
    private final TareasClient tareasClient;

    public CultivoService(CultivoRepository cultivoRepository, SectorRepository sectorRepository,
                          TareasClient tareasClient) {
        this.cultivoRepository = cultivoRepository;
        this.sectorRepository = sectorRepository;
        this.tareasClient = tareasClient;
    }

    // Si viene sectorId, solo los cultivos de ese sector (detalle del sector en la app)
    public List<Cultivo> listar(Long sectorId) {
        if (sectorId != null) return cultivoRepository.findBySectorId(sectorId);
        return cultivoRepository.findAllByOrderByNombreAsc();
    }

    public Cultivo buscar(Long id) {
        return cultivoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cultivo no encontrado"));
    }

    public Cultivo crear(Cultivo cultivo) {
        validar(cultivo);
        cultivo.setId(null);
        return cultivoRepository.save(cultivo);
    }

    public Cultivo actualizar(Long id, Cultivo datos) {
        buscar(id);
        validar(datos);
        datos.setId(id);
        return cultivoRepository.save(datos);
    }

    public void eliminar(Long id) {
        buscar(id);
        if (tareasClient.tieneReferencias(null, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El cultivo tiene tareas o reportes asociados");
        }
        cultivoRepository.deleteById(id);
    }

    // Mismas reglas que valida la app: el sector debe existir y las fechas deben tener sentido
    private void validar(Cultivo c) {
        if (!sectorRepository.existsById(c.getSectorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El sector indicado no existe");
        }
        if (c.getFechaCosecha() < c.getFechaSiembra()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La cosecha no puede ser anterior a la siembra");
        }
        Long trasplante = c.getFechaTrasplante();
        if (trasplante != null && (trasplante < c.getFechaSiembra() || trasplante > c.getFechaCosecha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El trasplante debe quedar entre la siembra y la cosecha");
        }
    }
}
