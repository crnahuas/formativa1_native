package cl.duoc.cdy2204.formativa.config;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.cdy2204.formativa.dto.ResumenInscripcionMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.MessageConverter;

class RabbitMQConfigTest {

    @Test
    void jacksonMessageConverterSePuedeInstanciar() {
        RabbitMQConfig config = new RabbitMQConfig();

        MessageConverter converter = config.jacksonMessageConverter(objectMapper());

        assertThat(converter).isNotNull();
    }

    @Test
    void jacksonMessageConverterSerializaLocalDateTime() {
        RabbitMQConfig config = new RabbitMQConfig();
        MessageConverter converter = config.jacksonMessageConverter(objectMapper());

        ResumenInscripcionMessage mensaje = new ResumenInscripcionMessage();
        mensaje.setNumeroResumen(10L);
        mensaje.setEstudianteNombre("Maria Perez");
        mensaje.setEstudianteEmail("maria.perez@duocuc.cl");
        mensaje.setFechaInscripcion(LocalDateTime.of(2026, 7, 18, 12, 0));
        mensaje.setTotal(new BigDecimal("120000"));
        mensaje.setContenidoResumen("contenido");

        assertThat(converter.toMessage(mensaje, null).getBody()).isNotEmpty();
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
