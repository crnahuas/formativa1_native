package cl.duoc.cdy2204.formativa.controller;

import cl.duoc.cdy2204.formativa.dto.S3Response;
import cl.duoc.cdy2204.formativa.service.AwsService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/s3")
public class AwsController {

    private final AwsService awsService;

    public AwsController(AwsService awsService) {
        this.awsService = awsService;
    }

    @PostMapping(value = "/uploadResumen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<S3Response> uploadResumen(
            @RequestParam @Positive Long numeroResumen,
            @RequestPart(required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(awsService.uploadResumen(numeroResumen, file));
    }

    @PostMapping("/uploadResumen")
    public ResponseEntity<S3Response> uploadResumenGenerado(@RequestParam @Positive Long numeroResumen) {
        return ResponseEntity.ok(awsService.uploadResumen(numeroResumen, null));
    }

    @PutMapping(value = "/updateResumen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<S3Response> updateResumen(
            @RequestParam @Positive Long numeroResumen,
            @RequestPart(required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(awsService.updateResumen(numeroResumen, file));
    }

    @PutMapping("/updateResumen")
    public ResponseEntity<S3Response> updateResumenGenerado(@RequestParam @Positive Long numeroResumen) {
        return ResponseEntity.ok(awsService.updateResumen(numeroResumen, null));
    }

    @GetMapping("/downloadResumen")
    public ResponseEntity<byte[]> downloadResumen(@RequestParam @Positive Long numeroResumen) {
        byte[] archivo = awsService.downloadResumen(numeroResumen);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("resumen-" + numeroResumen + ".txt")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(archivo);
    }

    @DeleteMapping("/deleteResumen")
    public ResponseEntity<S3Response> deleteResumen(@RequestParam @Positive Long numeroResumen) {
        return ResponseEntity.ok(awsService.deleteResumen(numeroResumen));
    }
}
