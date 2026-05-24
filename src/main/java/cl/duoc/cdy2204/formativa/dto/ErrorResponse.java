package cl.duoc.cdy2204.formativa.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {

    private LocalDateTime fecha;
    private int estado;
    private String error;
    private List<String> detalles;

    public ErrorResponse(LocalDateTime fecha, int estado, String error, List<String> detalles) {
        this.fecha = fecha;
        this.estado = estado;
        this.error = error;
        this.detalles = detalles;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getEstado() {
        return estado;
    }

    public String getError() {
        return error;
    }

    public List<String> getDetalles() {
        return detalles;
    }
}
