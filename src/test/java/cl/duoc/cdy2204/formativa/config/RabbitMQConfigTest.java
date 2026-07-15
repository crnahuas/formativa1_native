package cl.duoc.cdy2204.formativa.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.MessageConverter;

class RabbitMQConfigTest {

    @Test
    void jacksonMessageConverterSePuedeInstanciar() {
        RabbitMQConfig config = new RabbitMQConfig();

        MessageConverter converter = config.jacksonMessageConverter();

        assertThat(converter).isNotNull();
    }
}
