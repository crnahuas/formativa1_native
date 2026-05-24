package cl.duoc.cdy2204.formativa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import cl.duoc.cdy2204.formativa.dto.CursoRequest;
import cl.duoc.cdy2204.formativa.dto.CursoResponse;
import cl.duoc.cdy2204.formativa.entity.Curso;
import cl.duoc.cdy2204.formativa.repository.CursoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private CursoService cursoService;

    @Test
    void crearCursoPersisteYRetornaDatosDelCurso() {
        CursoRequest request = new CursoRequest();
        request.setNombre("Spring Boot Cloud Native");
        request.setInstructor("Docente CDY2204");
        request.setDuracion("24 horas");
        request.setCosto(new BigDecimal("120000"));

        Curso guardado = new Curso();
        guardado.setId(1L);
        guardado.setNombre(request.getNombre());
        guardado.setInstructor(request.getInstructor());
        guardado.setDuracion(request.getDuracion());
        guardado.setCosto(request.getCosto());

        when(cursoRepository.save(org.mockito.ArgumentMatchers.any(Curso.class))).thenReturn(guardado);

        CursoResponse response = cursoService.crearCurso(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Spring Boot Cloud Native");
        assertThat(response.getInstructor()).isEqualTo("Docente CDY2204");
        assertThat(response.getDuracion()).isEqualTo("24 horas");
        assertThat(response.getCosto()).isEqualByComparingTo("120000");
    }
}
