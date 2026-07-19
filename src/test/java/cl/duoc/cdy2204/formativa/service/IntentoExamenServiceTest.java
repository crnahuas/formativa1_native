package cl.duoc.cdy2204.formativa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import cl.duoc.cdy2204.formativa.dto.IntentoExamenFinalizarRequest;
import cl.duoc.cdy2204.formativa.dto.IntentoExamenInicioRequest;
import cl.duoc.cdy2204.formativa.dto.IntentoExamenResponse;
import cl.duoc.cdy2204.formativa.entity.Calificacion;
import cl.duoc.cdy2204.formativa.entity.Curso;
import cl.duoc.cdy2204.formativa.entity.Examen;
import cl.duoc.cdy2204.formativa.entity.Inscripcion;
import cl.duoc.cdy2204.formativa.entity.IntentoExamen;
import cl.duoc.cdy2204.formativa.repository.CalificacionRepository;
import cl.duoc.cdy2204.formativa.repository.ExamenRepository;
import cl.duoc.cdy2204.formativa.repository.InscripcionRepository;
import cl.duoc.cdy2204.formativa.repository.IntentoExamenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntentoExamenServiceTest {

    @Mock
    private IntentoExamenRepository intentoExamenRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private ExamenRepository examenRepository;

    @Mock
    private CalificacionRepository calificacionRepository;

    @Test
    void iniciarIntentoAsociaInscripcionYExamen() {
        Inscripcion inscripcion = inscripcion();
        Examen examen = examen();
        when(inscripcionRepository.findById(11L)).thenReturn(Optional.of(inscripcion));
        when(examenRepository.findById(30L)).thenReturn(Optional.of(examen));
        when(intentoExamenRepository.save(org.mockito.ArgumentMatchers.any(IntentoExamen.class)))
                .thenAnswer(invocation -> {
                    IntentoExamen intento = invocation.getArgument(0);
                    intento.setId(50L);
                    return intento;
                });

        IntentoExamenResponse response = service().iniciar(30L, inicioRequest());

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getInscripcionId()).isEqualTo(11L);
        assertThat(response.getExamenId()).isEqualTo(30L);
        assertThat(response.getEstado()).isEqualTo("INICIADO");
    }

    @Test
    void finalizarIntentoRegistraCalificacion() {
        IntentoExamen intento = intento();
        when(intentoExamenRepository.findById(50L)).thenReturn(Optional.of(intento));
        when(calificacionRepository.save(org.mockito.ArgumentMatchers.any(Calificacion.class)))
                .thenAnswer(invocation -> {
                    Calificacion calificacion = invocation.getArgument(0);
                    calificacion.setId(70L);
                    return calificacion;
                });
        when(intentoExamenRepository.save(org.mockito.ArgumentMatchers.any(IntentoExamen.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IntentoExamenResponse response = service().finalizar(50L, finalizarRequest());

        assertThat(response.getEstado()).isEqualTo("FINALIZADO");
        assertThat(response.getPuntajeObtenido()).isEqualByComparingTo("92");
        assertThat(response.getCalificacionId()).isEqualTo(70L);
        assertThat(response.getRespuestasJson()).contains("Arquitectura cloud native");
    }

    @Test
    void iniciarIntentoRechazaExamenDeCursoNoInscrito() {
        Inscripcion inscripcion = inscripcion();
        inscripcion.getCursos().clear();
        when(inscripcionRepository.findById(11L)).thenReturn(Optional.of(inscripcion));
        when(examenRepository.findById(30L)).thenReturn(Optional.of(examen()));

        assertThatThrownBy(() -> service().iniciar(30L, inicioRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La inscripcion no pertenece al curso");
    }

    private IntentoExamenService service() {
        return new IntentoExamenService(
                intentoExamenRepository,
                inscripcionRepository,
                examenRepository,
                calificacionRepository,
                new ObjectMapper()
        );
    }

    private IntentoExamenInicioRequest inicioRequest() {
        IntentoExamenInicioRequest request = new IntentoExamenInicioRequest();
        request.setInscripcionId(11L);
        return request;
    }

    private IntentoExamenFinalizarRequest finalizarRequest() {
        IntentoExamenFinalizarRequest request = new IntentoExamenFinalizarRequest();
        request.setPuntajeObtenido(new BigDecimal("92"));
        request.setRespuestas(List.of(Map.of(
                "pregunta", "Arquitectura cloud native",
                "respuesta", "API Gateway, BFF, RabbitMQ y S3"
        )));
        request.setComentario("Examen realizado");
        return request;
    }

    private IntentoExamen intento() {
        IntentoExamen intento = new IntentoExamen();
        intento.setId(50L);
        intento.setInscripcion(inscripcion());
        intento.setExamen(examen());
        intento.setEstado("INICIADO");
        intento.setFechaInicio(LocalDateTime.of(2026, 7, 19, 12, 0));
        return intento;
    }

    private Inscripcion inscripcion() {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(11L);
        inscripcion.setEstudianteNombre("Maria Perez");
        Curso curso = new Curso();
        curso.setId(10L);
        inscripcion.getCursos().add(curso);
        return inscripcion;
    }

    private Examen examen() {
        Curso curso = new Curso();
        curso.setId(10L);

        Examen examen = new Examen();
        examen.setId(30L);
        examen.setCurso(curso);
        examen.setTitulo("Evaluacion Cloud Native");
        examen.setPuntajeMaximo(new BigDecimal("100"));
        examen.setFechaDisponible(LocalDateTime.of(2026, 7, 19, 20, 0));
        return examen;
    }
}
