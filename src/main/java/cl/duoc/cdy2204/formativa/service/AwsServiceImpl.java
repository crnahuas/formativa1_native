package cl.duoc.cdy2204.formativa.service;

import cl.duoc.cdy2204.formativa.dto.S3Response;
import cl.duoc.cdy2204.formativa.exception.ArchivoStorageException;
import cl.duoc.cdy2204.formativa.exception.RecursoNoEncontradoException;
import cl.duoc.cdy2204.formativa.repository.S3Repository;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AwsServiceImpl implements AwsService {

    private static final String CONTENT_TYPE_TEXT = "text/plain; charset=UTF-8";

    private final S3Repository s3Repository;
    private final ResumenInscripcionService resumenInscripcionService;
    private final String bucketName;

    public AwsServiceImpl(
            S3Repository s3Repository,
            ResumenInscripcionService resumenInscripcionService,
            @Value("${aws.s3.bucket-name}") String bucketName
    ) {
        this.s3Repository = s3Repository;
        this.resumenInscripcionService = resumenInscripcionService;
        this.bucketName = bucketName;
    }

    @Override
    public S3Response uploadResumen(Long numeroResumen, MultipartFile file) {
        byte[] content = obtenerContenidoResumen(numeroResumen, file);
        String key = keyResumen(numeroResumen);
        s3Repository.upload(key, content, obtenerContentType(file));
        return response("Resumen subido correctamente a S3", numeroResumen, key);
    }

    @Override
    public S3Response updateResumen(Long numeroResumen, MultipartFile file) {
        String key = keyResumen(numeroResumen);
        validarExistencia(key, numeroResumen);
        byte[] content = obtenerContenidoResumen(numeroResumen, file);
        s3Repository.upload(key, content, obtenerContentType(file));
        return response("Resumen actualizado correctamente en S3", numeroResumen, key);
    }

    @Override
    public byte[] downloadResumen(Long numeroResumen) {
        String key = keyResumen(numeroResumen);
        validarExistencia(key, numeroResumen);
        return s3Repository.download(key);
    }

    @Override
    public S3Response deleteResumen(Long numeroResumen) {
        String key = keyResumen(numeroResumen);
        validarExistencia(key, numeroResumen);
        s3Repository.delete(key);
        return response("Resumen eliminado correctamente de S3", numeroResumen, key);
    }

    private byte[] obtenerContenidoResumen(Long numeroResumen, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return resumenInscripcionService.leerArchivoResumen(numeroResumen);
        }

        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new ArchivoStorageException("No fue posible leer el archivo recibido", exception);
        }
    }

    private void validarExistencia(String key, Long numeroResumen) {
        if (!s3Repository.exists(key)) {
            throw new RecursoNoEncontradoException("No existe resumen almacenado en S3 para el numero " + numeroResumen);
        }
    }

    private String obtenerContentType(MultipartFile file) {
        if (file == null || file.getContentType() == null || file.getContentType().isBlank()) {
            return CONTENT_TYPE_TEXT;
        }
        return file.getContentType();
    }

    private String keyResumen(Long numeroResumen) {
        return numeroResumen + "/" + ResumenInscripcionService.RESUMEN_FILENAME;
    }

    private S3Response response(String mensaje, Long numeroResumen, String key) {
        return new S3Response(
                mensaje,
                bucketName,
                numeroResumen + "/",
                ResumenInscripcionService.RESUMEN_FILENAME,
                key
        );
    }
}
