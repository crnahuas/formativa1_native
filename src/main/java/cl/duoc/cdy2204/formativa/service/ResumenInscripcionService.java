package cl.duoc.cdy2204.formativa.service;

import cl.duoc.cdy2204.formativa.entity.Curso;
import cl.duoc.cdy2204.formativa.entity.Inscripcion;
import cl.duoc.cdy2204.formativa.exception.ArchivoStorageException;
import cl.duoc.cdy2204.formativa.exception.RecursoNoEncontradoException;
import cl.duoc.cdy2204.formativa.repository.InscripcionRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumenInscripcionService {

    public static final String RESUMEN_FILENAME = "resumen.txt";

    private final InscripcionRepository inscripcionRepository;
    private final Path directorioResumenes;

    public ResumenInscripcionService(
            InscripcionRepository inscripcionRepository,
            @Value("${app.resumenes.path:./resumenes}") String resumenesPath
    ) {
        this.inscripcionRepository = inscripcionRepository;
        this.directorioResumenes = Path.of(resumenesPath);
    }

    @Transactional(readOnly = true)
    public Path generarArchivoResumen(Long numeroResumen) {
        Inscripcion inscripcion = inscripcionRepository.findById(numeroResumen)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la inscripcion " + numeroResumen));

        String contenido = generarContenido(inscripcion);
        Path carpetaResumen = directorioResumenes.resolve(String.valueOf(numeroResumen));
        Path archivoResumen = carpetaResumen.resolve(RESUMEN_FILENAME);

        try {
            Files.createDirectories(carpetaResumen);
            Files.writeString(archivoResumen, contenido, StandardCharsets.UTF_8);
            return archivoResumen;
        } catch (IOException exception) {
            throw new ArchivoStorageException("No fue posible generar el archivo fisico del resumen", exception);
        }
    }

    public byte[] leerArchivoResumen(Long numeroResumen) {
        Path archivo = generarArchivoResumen(numeroResumen);
        try {
            return Files.readAllBytes(archivo);
        } catch (IOException exception) {
            throw new ArchivoStorageException("No fue posible leer el archivo fisico del resumen", exception);
        }
    }

    private String generarContenido(Inscripcion inscripcion) {
        StringBuilder builder = new StringBuilder();
        builder.append("Resumen de inscripcion").append(System.lineSeparator());
        builder.append("Numero resumen: ").append(inscripcion.getId()).append(System.lineSeparator());
        builder.append("Estudiante: ").append(inscripcion.getEstudianteNombre()).append(System.lineSeparator());
        builder.append("Email: ").append(inscripcion.getEstudianteEmail()).append(System.lineSeparator());
        builder.append("Fecha: ").append(inscripcion.getFechaInscripcion()).append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("Cursos:").append(System.lineSeparator());

        for (Curso curso : inscripcion.getCursos()) {
            builder.append("- ")
                    .append(curso.getNombre())
                    .append(" | Instructor: ")
                    .append(curso.getInstructor())
                    .append(" | Duracion: ")
                    .append(curso.getDuracion())
                    .append(" | Costo: ")
                    .append(curso.getCosto())
                    .append(System.lineSeparator());
        }

        builder.append(System.lineSeparator());
        builder.append("Total a pagar: ").append(inscripcion.getTotal()).append(System.lineSeparator());
        return builder.toString();
    }
}
