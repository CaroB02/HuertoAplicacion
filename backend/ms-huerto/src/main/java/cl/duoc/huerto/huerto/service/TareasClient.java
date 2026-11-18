package cl.duoc.huerto.huerto.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

// COMUNICACIÓN ENTRE MICROSERVICIOS.
// Las tareas y los reportes viven en ms-tareas (otra base de datos), así que ms-huerto no puede
// usar una clave foránea para saber si un sector o cultivo está en uso. En su lugar le pregunta
// a ms-tareas por HTTP: GET http://localhost:8083/referencias?sectorId=1
@Component
public class TareasClient {

    private final RestClient restClient;

    // La URL viene de application.properties (servicios.tareas.url)
    public TareasClient(@Value("${servicios.tareas.url}") String urlTareas) {
        this.restClient = RestClient.builder().baseUrl(urlTareas).build();
    }

    // true si ms-tareas tiene tareas o reportes asociados a ese sector o cultivo
    public boolean tieneReferencias(Long sectorId, Long cultivoId) {
        try {
            Boolean respuesta = restClient.get()
                    .uri(uri -> {
                        uri.path("/referencias");
                        if (sectorId != null) uri.queryParam("sectorId", sectorId);
                        if (cultivoId != null) uri.queryParam("cultivoId", cultivoId);
                        return uri.build();
                    })
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(respuesta);
        } catch (RestClientException e) {
            // Si ms-tareas está apagado, NO borro: prefiero no dejar tareas apuntando a algo que no existe
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar ms-tareas para verificar si hay tareas asociadas");
        }
    }
}
