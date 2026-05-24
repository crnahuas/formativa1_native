package cl.duoc.cdy2204.formativa.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class InscripcionResponse {

    private Long inscripcionId;
    private String estudianteNombre;
    private String estudianteEmail;
    private LocalDateTime fechaInscripcion;
    private List<CursoResponse> cursosSeleccionados;
    private BigDecimal totalPagar;

    public InscripcionResponse(
            Long inscripcionId,
            String estudianteNombre,
            String estudianteEmail,
            LocalDateTime fechaInscripcion,
            List<CursoResponse> cursosSeleccionados,
            BigDecimal totalPagar
    ) {
        this.inscripcionId = inscripcionId;
        this.estudianteNombre = estudianteNombre;
        this.estudianteEmail = estudianteEmail;
        this.fechaInscripcion = fechaInscripcion;
        this.cursosSeleccionados = cursosSeleccionados;
        this.totalPagar = totalPagar;
    }

    public Long getInscripcionId() {
        return inscripcionId;
    }

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public String getEstudianteEmail() {
        return estudianteEmail;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public List<CursoResponse> getCursosSeleccionados() {
        return cursosSeleccionados;
    }

    public BigDecimal getTotalPagar() {
        return totalPagar;
    }
}
