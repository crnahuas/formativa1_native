package cl.duoc.cdy2204.formativa.service;

import cl.duoc.cdy2204.formativa.dto.CursoRequest;
import cl.duoc.cdy2204.formativa.dto.CursoResponse;
import cl.duoc.cdy2204.formativa.entity.Curso;
import cl.duoc.cdy2204.formativa.repository.CursoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listarCursos() {
        return cursoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CursoResponse crearCurso(CursoRequest request) {
        Curso curso = new Curso();
        curso.setNombre(request.getNombre());
        curso.setInstructor(request.getInstructor());
        curso.setDuracion(request.getDuracion());
        curso.setCosto(request.getCosto());

        Curso guardado = cursoRepository.save(curso);
        return toResponse(guardado);
    }

    public CursoResponse toResponse(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getInstructor(),
                curso.getDuracion(),
                curso.getCosto()
        );
    }
}
