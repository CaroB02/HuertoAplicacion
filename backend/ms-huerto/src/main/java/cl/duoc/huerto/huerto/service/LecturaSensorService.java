package cl.duoc.huerto.huerto.service;

import cl.duoc.huerto.huerto.model.LecturaSensor;
import cl.duoc.huerto.huerto.repository.LecturaSensorRepository;
import cl.duoc.huerto.huerto.repository.SectorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Lecturas SIMULADAS de sensores (simulación académica, no hay sensores reales).
@Service
public class LecturaSensorService {

    private final LecturaSensorRepository lecturaRepository;
    private final SectorRepository sectorRepository;

    public LecturaSensorService(LecturaSensorRepository lecturaRepository, SectorRepository sectorRepository) {
        this.lecturaRepository = lecturaRepository;
        this.sectorRepository = sectorRepository;
    }

    public List<LecturaSensor> listar() {
        return lecturaRepository.findAllByOrderByFechaDesc();
    }

    public LecturaSensor registrar(LecturaSensor lectura) {
        if (!sectorRepository.existsById(lectura.getSectorId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El sector indicado no existe");
        }
        lectura.setId(null);
        lectura.setOrigen("simulación académica");
        return lecturaRepository.save(lectura);
    }
}
