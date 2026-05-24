package cl.duoc.cdy2204.formativa.controller;

import cl.duoc.cdy2204.formativa.dto.InscripcionRequest;
import cl.duoc.cdy2204.formativa.dto.InscripcionResponse;
import cl.duoc.cdy2204.formativa.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping
    public ResponseEntity<InscripcionResponse> inscribir(@Valid @RequestBody InscripcionRequest request) {
        InscripcionResponse response = inscripcionService.inscribir(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
