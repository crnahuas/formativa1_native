package cl.duoc.cdy2204.formativa.repository;

import cl.duoc.cdy2204.formativa.entity.IntentoExamen;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoExamenRepository extends JpaRepository<IntentoExamen, Long> {

    List<IntentoExamen> findByInscripcionIdOrderByFechaInicioDesc(Long inscripcionId);
}
