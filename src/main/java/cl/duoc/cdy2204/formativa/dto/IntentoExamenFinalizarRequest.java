package cl.duoc.cdy2204.formativa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class IntentoExamenFinalizarRequest {

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal puntajeObtenido;

    @NotNull
    private Object respuestas;

    @Size(max = 500)
    private String comentario;

    public BigDecimal getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public void setPuntajeObtenido(BigDecimal puntajeObtenido) {
        this.puntajeObtenido = puntajeObtenido;
    }

    public Object getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(Object respuestas) {
        this.respuestas = respuestas;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
