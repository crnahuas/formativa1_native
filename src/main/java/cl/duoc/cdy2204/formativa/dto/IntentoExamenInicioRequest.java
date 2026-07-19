package cl.duoc.cdy2204.formativa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class IntentoExamenInicioRequest {

    @NotNull
    @Positive
    private Long inscripcionId;

    public Long getInscripcionId() {
        return inscripcionId;
    }

    public void setInscripcionId(Long inscripcionId) {
        this.inscripcionId = inscripcionId;
    }
}
