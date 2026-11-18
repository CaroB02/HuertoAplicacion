package cl.duoc.huerto.huerto.service;

import cl.duoc.huerto.huerto.model.Sector;
import cl.duoc.huerto.huerto.repository.CultivoRepository;
import cl.duoc.huerto.huerto.repository.LecturaSensorRepository;
import cl.duoc.huerto.huerto.repository.SectorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Reglas de negocio de los sectores.
@Service
public class SectorService {

    private final SectorRepository sectorRepository;
    private final CultivoRepository cultivoRepository;
    private final LecturaSensorRepository lecturaRepository;
    private final TareasClient tareasClient;

    public SectorService(SectorRepository sectorRepository, CultivoRepository cultivoRepository,
                         LecturaSensorRepository lecturaRepository, TareasClient tareasClient) {
        this.sectorRepository = sectorRepository;
        this.cultivoRepository = cultivoRepository;
        this.lecturaRepository = lecturaRepository;
        this.tareasClient = tareasClient;
    }

    public List<Sector> listar() {
        return sectorRepository.findAllByOrderByNombreAsc();
    }

    public Sector buscar(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sector no encontrado"));
    }

    public Sector crear(Sector sector) {
        sector.setId(null);   // el id lo asigna MySQL (AUTO_INCREMENT)
        return sectorRepository.save(sector);
    }

    public Sector actualizar(Long id, Sector datos) {
        buscar(id);           // 404 si no existe
        datos.setId(id);      // save() con un id existente hace UPDATE en vez de INSERT
        return sectorRepository.save(datos);
    }

    // No se puede borrar un sector que tiene cultivos (aquí) o tareas/reportes (en ms-tareas).
    // 409 Conflict = la petición es válida pero choca con el estado actual de los datos.
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        if (cultivoRepository.existsBySectorId(id) || tareasClient.tieneReferencias(id, null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El sector tiene cultivos, tareas o reportes asociados");
        }
        lecturaRepository.deleteBySectorId(id);
        sectorRepository.deleteById(id);
    }
}
