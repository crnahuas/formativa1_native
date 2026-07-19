package cl.duoc.cdy2204.formativa.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IntentoExamenResponse {

    private Long id;
    private Long inscripcionId;
    private Long examenId;
    private Long cursoId;
    private String estudianteNombre;
    private String examenTitulo;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;
    private BigDecimal puntajeObtenido;
    private BigDecimal puntajeMaximo;
    private Long calificacionId;
    private String respuestasJson;
    private String comentario;

    public IntentoExamenResponse(
            Long id,
            Long inscripcionId,
            Long examenId,
            Long cursoId,
            String estudianteNombre,
            String examenTitulo,
            String estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFinalizacion,
            BigDecimal puntajeObtenido,
            BigDecimal puntajeMaximo,
            Long calificacionId,
            String respuestasJson,
            String comentario
    ) {
        this.id = id;
        this.inscripcionId = inscripcionId;
        this.examenId = examenId;
        this.cursoId = cursoId;
        this.estudianteNombre = estudianteNombre;
        this.examenTitulo = examenTitulo;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFinalizacion = fechaFinalizacion;
        this.puntajeObtenido = puntajeObtenido;
        this.puntajeMaximo = puntajeMaximo;
        this.calificacionId = calificacionId;
        this.respuestasJson = respuestasJson;
        this.comentario = comentario;
    }

    public Long getId() {
        return id;
    }

    public Long getInscripcionId() {
        return inscripcionId;
    }

    public Long getExamenId() {
        return examenId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public String getExamenTitulo() {
        return examenTitulo;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public BigDecimal getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public BigDecimal getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public Long getCalificacionId() {
        return calificacionId;
    }

    public String getRespuestasJson() {
        return respuestasJson;
    }

    public String getComentario() {
        return comentario;
    }
}
