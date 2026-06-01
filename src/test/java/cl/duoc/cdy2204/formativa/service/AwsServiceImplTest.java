package cl.duoc.cdy2204.formativa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.cdy2204.formativa.dto.S3Response;
import cl.duoc.cdy2204.formativa.exception.RecursoNoEncontradoException;
import cl.duoc.cdy2204.formativa.repository.S3Repository;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AwsServiceImplTest {

    @Mock
    private S3Repository s3Repository;

    @Mock
    private ResumenInscripcionService resumenInscripcionService;

    @Test
    void uploadResumenUsaCarpetaConNumeroResumen() {
        AwsServiceImpl awsService = new AwsServiceImpl(s3Repository, resumenInscripcionService, "bucket-duoc");
        byte[] contenido = "resumen".getBytes(StandardCharsets.UTF_8);

        when(resumenInscripcionService.leerArchivoResumen(1001L)).thenReturn(contenido);

        S3Response response = awsService.uploadResumen(1001L, null);

        verify(s3Repository).upload("1001/resumen.txt", contenido, "text/plain; charset=UTF-8");
        assertThat(response.getCarpeta()).isEqualTo("1001/");
        assertThat(response.getClave()).isEqualTo("1001/resumen.txt");
    }

    @Test
    void updateResumenLanzaErrorSiNoExisteEnS3() {
        AwsServiceImpl awsService = new AwsServiceImpl(s3Repository, resumenInscripcionService, "bucket-duoc");

        when(s3Repository.exists("1001/resumen.txt")).thenReturn(false);

        assertThatThrownBy(() -> awsService.updateResumen(1001L, null))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe resumen almacenado en S3 para el numero 1001");
    }
}
