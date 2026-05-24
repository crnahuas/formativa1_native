package cl.duoc.cdy2204.formativa.dto;

import java.math.BigDecimal;

public class CursoResponse {

    private Long id;
    private String nombre;
    private String instructor;
    private String duracion;
    private BigDecimal costo;

    public CursoResponse(Long id, String nombre, String instructor, String duracion, BigDecimal costo) {
        this.id = id;
        this.nombre = nombre;
        this.instructor = instructor;
        this.duracion = duracion;
        this.costo = costo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getDuracion() {
        return duracion;
    }

    public BigDecimal getCosto() {
        return costo;
    }
}
