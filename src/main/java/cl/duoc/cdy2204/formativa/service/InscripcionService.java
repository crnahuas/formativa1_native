package cl.duoc.cdy2204.formativa.service;

import cl.duoc.cdy2204.formativa.dto.CursoResponse;
import cl.duoc.cdy2204.formativa.dto.InscripcionRequest;
import cl.duoc.cdy2204.formativa.dto.InscripcionResponse;
import cl.duoc.cdy2204.formativa.entity.Curso;
import cl.duoc.cdy2204.formativa.entity.Inscripcion;
import cl.duoc.cdy2204.formativa.exception.RecursoNoEncontradoException;
import cl.duoc.cdy2204.formativa.repository.CursoRepository;
import cl.duoc.cdy2204.formativa.repository.InscripcionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscripcionService {

    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CursoService cursoService;

    public InscripcionService(
            CursoRepository cursoRepository,
            InscripcionRepository inscripcionRepository,
            CursoService cursoService
    ) {
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.cursoService = cursoService;
    }

    @Transactional
    public InscripcionResponse inscribir(InscripcionRequest request) {
        List<Long> cursoIds = limpiarIdsDuplicados(request.getCursoIds());
        List<Curso> cursos = cursoRepository.findAllById(cursoIds);

        if (cursos.size() != cursoIds.size()) {
            throw new RecursoNoEncontradoException("Uno o mas cursos seleccionados no existen");
        }

        BigDecimal total = cursos.stream()
                .map(Curso::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudianteNombre(request.getEstudianteNombre());
        inscripcion.setEstudianteEmail(request.getEstudianteEmail());
        inscripcion.setFechaInscripcion(LocalDateTime.now());
        inscripcion.setCursos(cursos);
        inscripcion.setTotal(total);

        Inscripcion guardada = inscripcionRepository.save(inscripcion);
        List<CursoResponse> cursosSeleccionados = cursos.stream()
                .map(cursoService::toResponse)
                .toList();

        return new InscripcionResponse(
                guardada.getId(),
                guardada.getEstudianteNombre(),
                guardada.getEstudianteEmail(),
                guardada.getFechaInscripcion(),
                cursosSeleccionados,
                guardada.getTotal()
        );
    }

    private List<Long> limpiarIdsDuplicados(List<Long> ids) {
        Set<Long> unicos = new LinkedHashSet<>(ids);
        return unicos.stream().toList();
    }
}
