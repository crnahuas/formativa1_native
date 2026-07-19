package cl.duoc.cdy2204.formativa.service;

import cl.duoc.cdy2204.formativa.dto.IntentoExamenFinalizarRequest;
import cl.duoc.cdy2204.formativa.dto.IntentoExamenInicioRequest;
import cl.duoc.cdy2204.formativa.dto.IntentoExamenResponse;
import cl.duoc.cdy2204.formativa.entity.Calificacion;
import cl.duoc.cdy2204.formativa.entity.Examen;
import cl.duoc.cdy2204.formativa.entity.Inscripcion;
import cl.duoc.cdy2204.formativa.entity.IntentoExamen;
import cl.duoc.cdy2204.formativa.exception.RecursoNoEncontradoException;
import cl.duoc.cdy2204.formativa.repository.CalificacionRepository;
import cl.duoc.cdy2204.formativa.repository.ExamenRepository;
import cl.duoc.cdy2204.formativa.repository.InscripcionRepository;
import cl.duoc.cdy2204.formativa.repository.IntentoExamenRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntentoExamenService {

    private static final String ESTADO_INICIADO = "INICIADO";
    private static final String ESTADO_FINALIZADO = "FINALIZADO";

    private final IntentoExamenRepository intentoExamenRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ExamenRepository examenRepository;
    private final CalificacionRepository calificacionRepository;
    private final ObjectMapper objectMapper;

    public IntentoExamenService(
            IntentoExamenRepository intentoExamenRepository,
            InscripcionRepository inscripcionRepository,
            ExamenRepository examenRepository,
            CalificacionRepository calificacionRepository,
            ObjectMapper objectMapper
    ) {
        this.intentoExamenRepository = intentoExamenRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.examenRepository = examenRepository;
        this.calificacionRepository = calificacionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public IntentoExamenResponse iniciar(Long examenId, IntentoExamenInicioRequest request) {
        Inscripcion inscripcion = verificarInscripcion(request.getInscripcionId());
        Examen examen = verificarExamen(examenId);
        validarInscripcionCurso(inscripcion, examen);

        IntentoExamen intento = new IntentoExamen();
        intento.setInscripcion(inscripcion);
        intento.setExamen(examen);
        intento.setEstado(ESTADO_INICIADO);
        intento.setFechaInicio(LocalDateTime.now());

        return toResponse(intentoExamenRepository.save(intento), null);
    }

    @Transactional
    public IntentoExamenResponse finalizar(Long intentoId, IntentoExamenFinalizarRequest request) {
        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el intento de examen " + intentoId));
        if (ESTADO_FINALIZADO.equals(intento.getEstado())) {
            throw new IllegalArgumentException("El intento de examen ya fue finalizado");
        }
        if (request.getPuntajeObtenido().compareTo(intento.getExamen().getPuntajeMaximo()) > 0) {
            throw new IllegalArgumentException("El puntaje obtenido no puede superar el maximo del examen");
        }

        intento.setEstado(ESTADO_FINALIZADO);
        intento.setFechaFinalizacion(LocalDateTime.now());
        intento.setPuntajeObtenido(request.getPuntajeObtenido());
        intento.setComentario(request.getComentario());
        intento.setRespuestasJson(toJson(request.getRespuestas()));

        Calificacion calificacion = new Calificacion();
        calificacion.setInscripcion(intento.getInscripcion());
        calificacion.setExamen(intento.getExamen());
        calificacion.setPuntaje(request.getPuntajeObtenido());
        calificacion.setComentario(request.getComentario());
        calificacion.setFechaRegistro(LocalDateTime.now());
        Calificacion guardada = calificacionRepository.save(calificacion);

        return toResponse(intentoExamenRepository.save(intento), guardada.getId());
    }

    @Transactional(readOnly = true)
    public List<IntentoExamenResponse> listarPorInscripcion(Long inscripcionId) {
        verificarInscripcion(inscripcionId);
        return intentoExamenRepository.findByInscripcionIdOrderByFechaInicioDesc(inscripcionId)
                .stream()
                .map(intento -> toResponse(intento, null))
                .toList();
    }

    private Inscripcion verificarInscripcion(Long inscripcionId) {
        return inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la inscripcion " + inscripcionId));
    }

    private Examen verificarExamen(Long examenId) {
        return examenRepository.findById(examenId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el examen " + examenId));
    }

    private void validarInscripcionCurso(Inscripcion inscripcion, Examen examen) {
        Long cursoExamenId = examen.getCurso().getId();
        boolean cursoInscrito = inscripcion.getCursos().stream()
                .anyMatch(curso -> curso.getId().equals(cursoExamenId));
        if (!cursoInscrito) {
            throw new IllegalArgumentException("La inscripcion no pertenece al curso del examen");
        }
    }

    private String toJson(Object respuestas) {
        try {
            return objectMapper.writeValueAsString(respuestas);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Las respuestas del examen no tienen formato JSON valido", exception);
        }
    }

    private IntentoExamenResponse toResponse(IntentoExamen intento, Long calificacionId) {
        return new IntentoExamenResponse(
                intento.getId(),
                intento.getInscripcion().getId(),
                intento.getExamen().getId(),
                intento.getExamen().getCurso().getId(),
                intento.getInscripcion().getEstudianteNombre(),
                intento.getExamen().getTitulo(),
                intento.getEstado(),
                intento.getFechaInicio(),
                intento.getFechaFinalizacion(),
                intento.getPuntajeObtenido(),
                intento.getExamen().getPuntajeMaximo(),
                calificacionId,
                intento.getRespuestasJson(),
                intento.getComentario()
        );
    }
}
