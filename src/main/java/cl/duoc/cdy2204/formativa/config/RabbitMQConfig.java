package cl.duoc.cdy2204.formativa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RESUMEN_QUEUE = "resumen.inscripcion.queue";
    public static final String RESUMEN_EXCHANGE = "resumen.inscripcion.exchange";
    public static final String RESUMEN_ROUTING_KEY = "resumen.inscripcion.key";

    @Bean
    Queue resumenQueue(@Value("${app.rabbitmq.resumen.queue:" + RESUMEN_QUEUE + "}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    DirectExchange resumenExchange(
            @Value("${app.rabbitmq.resumen.exchange:" + RESUMEN_EXCHANGE + "}") String exchangeName
    ) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Binding resumenBinding(
            Queue resumenQueue,
            DirectExchange resumenExchange,
            @Value("${app.rabbitmq.resumen.routing-key:" + RESUMEN_ROUTING_KEY + "}") String routingKey
    ) {
        return BindingBuilder
                .bind(resumenQueue)
                .to(resumenExchange)
                .with(routingKey);
    }

    @Bean
    MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
