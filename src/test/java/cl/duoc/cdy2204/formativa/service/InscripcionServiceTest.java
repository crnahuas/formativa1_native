package cl.duoc.cdy2204.formativa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import cl.duoc.cdy2204.formativa.dto.InscripcionRequest;
import cl.duoc.cdy2204.formativa.dto.InscripcionResponse;
import cl.duoc.cdy2204.formativa.entity.Curso;
import cl.duoc.cdy2204.formativa.entity.Inscripcion;
import cl.duoc.cdy2204.formativa.exception.RecursoNoEncontradoException;
import cl.duoc.cdy2204.formativa.repository.CursoRepository;
import cl.duoc.cdy2204.formativa.repository.InscripcionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    private InscripcionService inscripcionService;

    @BeforeEach
    void setUp() {
        CursoService cursoService = new CursoService(cursoRepository);
        inscripcionService = new InscripcionService(cursoRepository, inscripcionRepository, cursoService);
    }

    @Test
    void inscribirCalculaTotalYGuardaInscripcion() {
        Curso curso1 = curso(1L, "Spring Boot", "Docente 1", "12 horas", "100000");
        Curso curso2 = curso(2L, "Docker", "Docente 2", "8 horas", "50000");

        InscripcionRequest request = new InscripcionRequest();
        request.setEstudianteNombre("Maria Perez");
        request.setEstudianteEmail("maria.perez@duocuc.cl");
        request.setCursoIds(List.of(1L, 2L));

        when(cursoRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(curso1, curso2));
        when(inscripcionRepository.save(org.mockito.ArgumentMatchers.any(Inscripcion.class)))
                .thenAnswer(invocation -> {
                    Inscripcion inscripcion = invocation.getArgument(0);
                    inscripcion.setId(1L);
                    inscripcion.setFechaInscripcion(LocalDateTime.now());
                    return inscripcion;
                });
        InscripcionResponse response = inscripcionService.inscribir(request);

        assertThat(response.getInscripcionId()).isEqualTo(1L);
        assertThat(response.getCursosSeleccionados()).hasSize(2);
        assertThat(response.getTotalPagar()).isEqualByComparingTo("150000");
    }

    @Test
    void inscribirLanzaErrorCuandoCursoNoExiste() {
        InscripcionRequest request = new InscripcionRequest();
        request.setEstudianteNombre("Maria Perez");
        request.setEstudianteEmail("maria.perez@duocuc.cl");
        request.setCursoIds(List.of(99L));

        when(cursoRepository.findAllById(List.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> inscripcionService.inscribir(request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Uno o mas cursos seleccionados no existen");
    }

    private Curso curso(Long id, String nombre, String instructor, String duracion, String costo) {
        Curso curso = new Curso();
        curso.setId(id);
        curso.setNombre(nombre);
        curso.setInstructor(instructor);
        curso.setDuracion(duracion);
        curso.setCosto(new BigDecimal(costo));
        return curso;
    }
}
