package cl.duoc.cdy2204.formativa.controller;

import cl.duoc.cdy2204.formativa.dto.InscripcionRequest;
import cl.duoc.cdy2204.formativa.dto.InscripcionResponse;
import cl.duoc.cdy2204.formativa.service.InscripcionService;
import cl.duoc.cdy2204.formativa.service.ResumenInscripcionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final ResumenInscripcionService resumenInscripcionService;

    public InscripcionController(
            InscripcionService inscripcionService,
            ResumenInscripcionService resumenInscripcionService
    ) {
        this.inscripcionService = inscripcionService;
        this.resumenInscripcionService = resumenInscripcionService;
    }

    @PostMapping
    public ResponseEntity<InscripcionResponse> inscribir(@Valid @RequestBody InscripcionRequest request) {
        InscripcionResponse response = inscripcionService.inscribir(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{numeroResumen}/resumen")
    public ResponseEntity<byte[]> descargarResumen(@PathVariable @Positive Long numeroResumen) {
        byte[] archivo = resumenInscripcionService.leerArchivoResumen(numeroResumen);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("resumen-" + numeroResumen + ".txt")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(archivo);
    }
}
