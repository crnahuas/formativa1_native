package cl.duoc.cdy2204.formativa.exception;

import cl.duoc.cdy2204.formativa.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> detalles = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud invalida", detalles);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> detalles = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud invalida", detalles);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RecursoNoEncontradoException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(exception.getMessage()));
    }

    @ExceptionHandler(ArchivoStorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(ArchivoStorageException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de archivo", List.of(exception.getMessage()));
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ErrorResponse> handleS3(S3Exception exception) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        if (exception.statusCode() == 403) {
            status = HttpStatus.FORBIDDEN;
        } else if (exception.statusCode() == 404) {
            status = HttpStatus.NOT_FOUND;
        } else if (exception.statusCode() == 400) {
            status = HttpStatus.BAD_REQUEST;
        }

        String awsMessage = exception.awsErrorDetails() == null
                ? exception.getMessage()
                : exception.awsErrorDetails().errorMessage();

        return buildResponse(
                status,
                "Error AWS S3",
                List.of("AWS S3 respondio " + exception.statusCode() + ": " + awsMessage)
        );
    }

    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<ErrorResponse> handleAwsClient(SdkClientException exception) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                "Error conexion AWS",
                List.of(exception.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                List.of("No fue posible procesar la solicitud")
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, List<String> detalles) {
        ErrorResponse response = new ErrorResponse(LocalDateTime.now(), status.value(), error, detalles);
        return ResponseEntity.status(status).body(response);
    }
}
