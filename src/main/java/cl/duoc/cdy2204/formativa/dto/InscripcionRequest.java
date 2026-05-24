package cl.duoc.cdy2204.formativa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class InscripcionRequest {

    @NotBlank(message = "El nombre del estudiante es obligatorio")
    private String estudianteNombre;

    @NotBlank(message = "El email del estudiante es obligatorio")
    @Email(message = "El email del estudiante no tiene un formato valido")
    private String estudianteEmail;

    @NotEmpty(message = "Debe seleccionar al menos un curso")
    private List<Long> cursoIds;

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public void setEstudianteNombre(String estudianteNombre) {
        this.estudianteNombre = estudianteNombre;
    }

    public String getEstudianteEmail() {
        return estudianteEmail;
    }

    public void setEstudianteEmail(String estudianteEmail) {
        this.estudianteEmail = estudianteEmail;
    }

    public List<Long> getCursoIds() {
        return cursoIds;
    }

    public void setCursoIds(List<Long> cursoIds) {
        this.cursoIds = cursoIds;
    }
}
