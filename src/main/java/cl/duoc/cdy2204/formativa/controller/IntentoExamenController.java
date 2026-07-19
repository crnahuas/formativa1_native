package cl.duoc.cdy2204.formativa.controller;

import cl.duoc.cdy2204.formativa.dto.IntentoExamenFinalizarRequest;
import cl.duoc.cdy2204.formativa.dto.IntentoExamenInicioRequest;
import cl.duoc.cdy2204.formativa.dto.IntentoExamenResponse;
import cl.duoc.cdy2204.formativa.service.IntentoExamenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class IntentoExamenController {

    private final IntentoExamenService intentoExamenService;

    public IntentoExamenController(IntentoExamenService intentoExamenService) {
        this.intentoExamenService = intentoExamenService;
    }

    @PostMapping("/examenes/{examenId}/intentos")
    public ResponseEntity<IntentoExamenResponse> iniciar(
            @PathVariable @Positive Long examenId,
            @Valid @RequestBody IntentoExamenInicioRequest request
    ) {
        IntentoExamenResponse response = intentoExamenService.iniciar(examenId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/intentos/{intentoId}/finalizar")
    public ResponseEntity<IntentoExamenResponse> finalizar(
            @PathVariable @Positive Long intentoId,
            @Valid @RequestBody IntentoExamenFinalizarRequest request
    ) {
        return ResponseEntity.ok(intentoExamenService.finalizar(intentoId, request));
    }

    @GetMapping("/inscripciones/{inscripcionId}/intentos")
    public ResponseEntity<List<IntentoExamenResponse>> listarPorInscripcion(
            @PathVariable @Positive Long inscripcionId
    ) {
        return ResponseEntity.ok(intentoExamenService.listarPorInscripcion(inscripcionId));
    }
}
